package com.witelokk.musicapp.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun DeletePlaylistDialog(
    showDialog: Boolean,
    playlistName: String,
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    if (!showDialog) return

    AlertDialog(
        title = { Text(stringResource(Res.string.delete_playlists_dialog_title)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(onClick = onConfirmDelete) {
                Text(stringResource(Res.string.yes))
            }
        },
        text = { Text(stringResource(Res.string.delete_playlists_dialog_text, playlistName)) },
    )
}

