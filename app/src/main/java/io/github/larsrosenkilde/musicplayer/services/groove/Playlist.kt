package io.github.larsrosenkilde.musicplayer.services.groove

import android.net.Uri
import androidx.compose.runtime.Immutable
import coil.request.ImageRequest
import io.github.larsrosenkilde.musicplayer.MusicPlayer
import io.github.larsrosenkilde.musicplayer.services.parsers.M3U
import io.github.larsrosenkilde.musicplayer.ui.helpers.Assets
import org.json.JSONArray
import org.json.JSONObject

@Immutable
data class Playlist(
    val id: String,
    val title: String,
    val songs: List<Long>,
    val numberOfTracks: Int,
    val local: Local?
) {
    data class Local(val id: Long, val path: String, val uri: Uri) {
        fun toJSONObject(): JSONObject {
            val json = JSONObject()
            json.put(PLAYLIST_LOCAL_ID_KEY, id)
            json.put(PLAYLIST_LOCAL_PATH_KEY, path)
            json.put(PLAYLIST_LOCAL_URI_KEY, uri.toString())
            return json
        }

        companion object {
            const val PLAYLIST_LOCAL_ID_KEY = "l_id"
            const val PLAYLIST_LOCAL_PATH_KEY = "l_path"
            const val PLAYLIST_LOCAL_URI_KEY = "l_uri"

            fun fromJSONObject(serialized: JSONObject): Local {
                return Local(
                    id = serialized.getLong(PLAYLIST_LOCAL_ID_KEY),
                    path = serialized.getString(PLAYLIST_LOCAL_PATH_KEY),
                    uri = Uri.parse(serialized.getString(PLAYLIST_LOCAL_URI_KEY))
                )
            }
        }
    }

    fun createArtworkImageRequest(musicPlayer: MusicPlayer) =
        songs.firstOrNull()
            ?.let { musicPlayer.groove.song.getSongWithId(it)?.createArtworkImageRequest(musicPlayer) }
            ?: ImageRequest.Builder(musicPlayer.applicationContext)
                .data(Assets.getPlaceholderUri(musicPlayer.applicationContext))

    fun isLocal() = local != null

    fun toJSONObject(): JSONObject {
        val json = JSONObject()
        json.put(PLAYLIST_ID_KEY, id)
        json.put(PLAYLIST_TITLE_KEY, title)
        json.put(PLAYLIST_SONGS_KEY, JSONArray(songs))
        json.put(PLAYLIST_NUMBER_OF_TRACKS_KEY, numberOfTracks)
        return json
    }

    companion object {
        const val PLAYLIST_ID_KEY = "id"
        const val PLAYLIST_TITLE_KEY = "title"
        const val PLAYLIST_SONGS_KEY = "songs"
        const val PLAYLIST_NUMBER_OF_TRACKS_KEY = "n_tracks"

        fun fromJSONObject(serialized: JSONObject): Playlist {
            val songs = serialized.getJSONArray(PLAYLIST_SONGS_KEY).let {
                MutableList(it.length()) { i -> it.getLong(i) }.toList()
            }
            return Playlist(
                id = serialized.getString(PLAYLIST_ID_KEY),
                title = serialized.getString(PLAYLIST_TITLE_KEY),
                songs = songs,
                numberOfTracks = serialized.getInt(PLAYLIST_NUMBER_OF_TRACKS_KEY),
                local = null
            )
        }

        fun fromM3U(musicPlayer: MusicPlayer, local: Local): Playlist {
            val path = GrooveExplorer.Path(local.path)
            val dir = path.dirname
            val content = musicPlayer.applicationContext.contentResolver
                .openInputStream(local.uri)
                ?.use { String(it.readBytes()) } ?: ""
            val m3u = M3U.parse(content)
            val songs = mutableListOf<Long>()
            m3u.entries.forEach { entry ->
                val resolvedPath = dir.resolve(GrooveExplorer.Path(entry.path)).toString()
                val id = musicPlayer.groove.song.cachedPaths[resolvedPath]
                id?.let { songs.add(it) }
            }
            return Playlist(
                id = local.path,
                title = path.basename.removeSuffix(".m3u"),
                songs = songs,
                numberOfTracks = songs.size,
                local = local
            )
        }
    }
}