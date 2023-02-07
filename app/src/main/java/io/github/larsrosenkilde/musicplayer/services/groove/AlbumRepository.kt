package io.github.larsrosenkilde.musicplayer.services.groove

import android.content.ContentUris
import android.provider.MediaStore
import io.github.larsrosenkilde.musicplayer.MusicPlayer
import io.github.larsrosenkilde.musicplayer.ui.helpers.Assets
import io.github.larsrosenkilde.musicplayer.ui.helpers.createHandyImageRequest
import io.github.larsrosenkilde.musicplayer.utils.*
import java.util.concurrent.ConcurrentHashMap

enum class AlbumSortBy {
    ALBUM_NAME,
    ARTIST_NAME,
    TRACKS_COUNT
}

class AlbumRepository(private val musicPlayer: MusicPlayer) {
    private val cached = ConcurrentHashMap<Long, Album>()
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()

    private val searcher = FuzzySearcher<Album>(
        options = listOf(
            FuzzySearchOption({ it.name }, 3),
            FuzzySearchOption({ it.artist })
        )
    )

    fun fetch() {
        if (isUpdating) return
        isUpdating = true
        cached.clear()
        onUpdate.dispatch(null)
        val cursor = musicPlayer.applicationContext.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Albums.ALBUM + " ASC"
        )
        try {
            val updateDispatcher = GrooveRepositoryUpdateDispatcher {
                onUpdate.dispatch(null)
            }
            cursor?.use {
                while (it.moveToNext()) {
                    kotlin
                        .runCatching { Album.fromCursor(it) }
                        .getOrNull()
                        ?.let { album ->
                            cached[album.id] = album
                            updateDispatcher.increment()
                        }
                }
            }
        } catch (err: Exception) {
            //Logger.error("AlbumRepository", "fetch failed: $err")
        }
    }

    fun getDefaultAlbumArtworkUri() = Assets.getPlaceholderUri(musicPlayer.applicationContext)

    fun getAlbumWithId(albumId: Long) = cached[albumId]

    fun getAlbumOfArtist(artistName: String) = cached.values.find {
        it.artist == artistName
    }

    fun getAlbumsOfArtist(artistName: String) = cached.values.filter {
        it.artist == artistName
    }

    fun getAlbumsOfAlbumArtist(artistName: String) =
        musicPlayer.groove.song.getAlbumIdsOfAlbumArtist(artistName)
            .mapNotNull { getAlbumWithId(it) }

    fun getAlbumArtworkUri(albumId: Long) = ContentUris.withAppendedId(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        albumId
    )

    fun createAlbumArtworkImageRequest(albumId: Long) = createHandyImageRequest(
        musicPlayer.applicationContext,
        image = getAlbumArtworkUri(albumId),
        fallback = Assets.placeholderId
    )

    fun getAll() = cached.values.toList()
}