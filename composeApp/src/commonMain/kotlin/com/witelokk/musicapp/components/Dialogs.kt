package com.witelokk.musicapp.components

import androidx.compose.runtime.Composable
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song

@Composable
expect fun AddToPlaylistsDialog(
    showDialog: Boolean,
    playlists: List<PlaylistSummary>,
    onDismissRequest: () -> Unit,
    onAddRequest: (List<String>) -> Unit,
)

@Composable
expect fun CreatePlaylistDialog(
    showDialog: Boolean,
    playlistName: String,
    onNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onCreate: () -> Unit,
)

@Composable
expect fun DeletePlaylistDialog(
    showDialog: Boolean,
    playlistName: String,
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit,
)

@Composable
expect fun EditPlaylistNameDialog(
    showDialog: Boolean,
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
)

@Composable
expect fun ArtistsDialog(
    showDialog: Boolean,
    song: Song,
    onArtistSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
)

