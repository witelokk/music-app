package com.witelokk.musicapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.witelokk.musicapp.api.models.Song
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ArtistsDialog(
    showDialog: Boolean,
    song: Song,
    onArtistSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.artists)) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(Res.string.close)) }
        },
        text = {
            LazyColumn {
                items(song.artists) { artist ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                onArtistSelected(artist.id)
                            }
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        coil3.compose.AsyncImage(
                            model = artist.avatarUrl,
                            contentDescription = "",
                            error = coil3.compose.rememberAsyncImagePainter(Res.drawable.artist_placeholder),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(100)),
                        )
                        androidx.compose.foundation.layout.Spacer(
                            modifier = Modifier.size(16.dp)
                        )
                        Text(artist.name)
                    }
                }
            }
        },
    )
}

