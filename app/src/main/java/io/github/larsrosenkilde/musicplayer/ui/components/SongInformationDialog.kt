package io.github.larsrosenkilde.musicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.larsrosenkilde.musicplayer.services.groove.Song
import io.github.larsrosenkilde.musicplayer.ui.helpers.ViewContext
import io.github.larsrosenkilde.musicplayer.utils.DurationFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@Composable
fun SongInformationDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    context.musicPlayer.t.details,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(20.dp, 0.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Column(
                    modifier = Modifier.padding(16.dp, 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KeyValueTextComponent(
                        context.musicPlayer.t.trackName,
                        song.title
                    )
                    KeyValueTextComponent(
                        context.musicPlayer.t.artist,
                        song.artistName ?: context.musicPlayer.t.unk
                    )
                    KeyValueTextComponent(
                        context.musicPlayer.t.albumArtist,
                        song.additional.albumArtist ?: context.musicPlayer.t.unk
                    )
                    KeyValueTextComponent(
                        context.musicPlayer.t.composer,
                        song.composer ?: context.musicPlayer.t.unk
                    )
                    song.year?.let {
                        KeyValueTextComponent(context.musicPlayer.t.year, it.toString())
                    }
                    KeyValueTextComponent(
                        context.musicPlayer.t.duration,
                        DurationFormatter.formatAsMS(song.duration)
                    )
                    song.additional.genre?.let {
                        KeyValueTextComponent(context.musicPlayer.t.genre, it)
                    }
                    song.additional.bitrate?.let {
                        KeyValueTextComponent(
                            context.musicPlayer.t.bitrate,
                            context.musicPlayer.t.XKbps(it / 1000)
                        )
                    }
                    KeyValueTextComponent(
                        context.musicPlayer.t.filename,
                        song.filename
                    )
                    KeyValueTextComponent(
                        context.musicPlayer.t.path,
                        song.path
                    )
                    KeyValueTextComponent(
                        context.musicPlayer.t.size,
                        "${round((song.size / 1024 / 1024).toDouble())} MB"
                    )
                    KeyValueTextComponent(
                        context.musicPlayer.t.dateAdded,
                        SimpleDateFormat.getInstance()
                            .format(Date(song.dateAdded))
                    )
                    KeyValueTextComponent(
                        context.musicPlayer.t.lastModified,
                        SimpleDateFormat.getInstance()
                            .format(Date(song.dateModified))
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyValueTextComponent(key: String, value: String) {
    Column {
        Text(
            key,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}