package com.witelokk.musicapp.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun CreatePlaylistDialog(
    showDialog: Boolean,
    playlistName: String,
    onNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onCreate: () -> Unit,
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.create_playlist)) },
        confirmButton = {
            TextButton(
                onClick = onCreate,
                enabled = playlistName.isNotEmpty()
            ) { Text(stringResource(Res.string.create)) }
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = playlistName,
                onValueChange = onNameChange,
            )
        },
    )
}

