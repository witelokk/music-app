package com.witelokk.musicapp.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun EditPlaylistNameDialog(
    showDialog: Boolean,
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!showDialog) return

    AlertDialog(
        title = { Text(stringResource(Res.string.edit_playlist_name_dialog_title)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.yes))
            }
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
            )
        },
    )
}

