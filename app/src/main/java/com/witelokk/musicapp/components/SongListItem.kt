package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R
import com.witelokk.musicapp.api.models.Song
import kotlin.time.Duration.Companion.seconds

@Composable
fun SongListItem(
    song: Song,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true,
    showDuration: Boolean = false,
    isPlaying: Boolean = false,
    onFavoriteClick: () -> Unit = {},
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            song.coverUrl,
            "Cover",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp)),
            error = painterResource(R.drawable.artist_placeholder)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Unspecified,
                modifier = Modifier.height(24.dp)
            )
            if (showDuration) {
                Text(
                    formatDuration(song.durationSeconds.seconds),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    song.artists.map { it.name }.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (showFavorite) {
            IconButton(onClick = onFavoriteClick, modifier = Modifier.offset(x = 16.dp)) {
                Icon(
                    if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        IconButton(onClick = {}, modifier = Modifier.offset(x = 16.dp)) {
            Icon(Icons.Default.MoreVert, "More")
        }
    }
}