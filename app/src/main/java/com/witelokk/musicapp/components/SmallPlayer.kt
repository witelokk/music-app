package com.witelokk.musicapp.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R
import com.witelokk.musicapp.data.PlayerState

@Composable
fun SmallPlayer(playerState: PlayerState, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                playerState.song.cover,
                modifier = Modifier
                    .height(70.dp)
                    .clip(RoundedCornerShape(12)),
                contentDescription = null, // decorative element
                error = painterResource(R.drawable.artist_placeholder)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            playerState.song.name,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            playerState.song.artistName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            if (playerState.song.liked) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = null,
                            tint = if (playerState.song.liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            if (playerState.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (1f * playerState.currentPosition.inWholeSeconds) / playerState.song.duration.inWholeSeconds },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                )
            }
        }
    }
}
