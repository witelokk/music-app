package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.witelokk.musicapp.api.models.PlaylistSummary

@Composable
fun AddToPlaylistsDialog(
    playlists: List<PlaylistSummary>,
    onDismissRequest: () -> Unit,
    onAddRequest: (List<String>) -> Unit,
) {
    val selectedPlaylists = rememberSaveable { mutableStateOf(setOf<String>()) }

    AlertDialog(
        title = { Text("Add to playlists") },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                enabled = (playlists.isNotEmpty() && selectedPlaylists.value.isNotEmpty()),
                onClick = { onAddRequest(selectedPlaylists.value.toList()) }
            ) { Text("Add") }
        },
        text = {
            Column {
                if (playlists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                LazyColumn {
                    items(playlists) { playlist ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = playlist.id in selectedPlaylists.value,
                                onCheckedChange = { checked ->
                                    selectedPlaylists.value = if (checked) {
                                        selectedPlaylists.value + playlist.id
                                    } else {
                                        selectedPlaylists.value - playlist.id
                                    }
                                }
                            )
                            Text(playlist.name)
                        }
                    }
                }
            }
        }
    )
}