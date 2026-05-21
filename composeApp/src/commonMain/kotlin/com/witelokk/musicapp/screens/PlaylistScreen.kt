package com.witelokk.musicapp.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.witelokk.musicapp.components.DeletePlaylistDialog
import com.witelokk.musicapp.components.EditPlaylistNameDialog
import com.witelokk.musicapp.components.RequestFailedContent
import kotlinx.serialization.Serializable
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.connection_failed
import musicapp.composeapp.generated.resources.delete
import musicapp.composeapp.generated.resources.edit
import musicapp.composeapp.generated.resources.load_failed
import musicapp.composeapp.generated.resources.remove_from_playlist
import org.jetbrains.compose.resources.stringResource
import com.witelokk.musicapp.viewmodel.PlaylistScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class PlaylistScreenRoute(
    val id: String
)

@Composable
fun PlaylistScreen(
    navController: NavController,
    route: PlaylistScreenRoute,
    viewModel: PlaylistScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val loadFailedMessage = stringResource(Res.string.load_failed)
    val connectionFailedMessage = stringResource(Res.string.connection_failed)

    var showDeletePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showEditPlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var playlistMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var newName by remember { mutableStateOf(state.name) }

    LaunchedEffect(Unit) {
        viewModel.loadPlaylist(route.id)
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            navController.navigateUp()
        }
    }

    DeletePlaylistDialog(
        showDialog = showDeletePlaylistDialog,
        playlistName = state.name,
        onDismissRequest = { showDeletePlaylistDialog = false },
        onConfirmDelete = {
            viewModel.deletePlaylist()
            showDeletePlaylistDialog = false
        },
    )

    EditPlaylistNameDialog(
        showDialog = showEditPlaylistDialog,
        currentName = newName,
        onNameChange = { newName = it },
        onDismissRequest = { showEditPlaylistDialog = false },
        onConfirm = {
            viewModel.editPlaylistName(newName)
            showEditPlaylistDialog = false
        },
    )

    SongListScreen(
        navController = navController,
        title = state.name,
        songs = state.songs,
        playlists = state.playlists,
        playerState = state.playerState,
        isLoading = state.isLoading,
        showContent = !state.isLoading,
        songCacheState = viewModel::songCacheState,
        onLoadPlaylists = viewModel::loadPlaylists,
        onPlayAllSongs = { offline -> viewModel.playAllSongs(offline) },
        onShufflePlayAllSongs = { offline -> viewModel.shufflePlayAllSongs(offline) },
        onSongClick = { song, offline -> viewModel.playSong(song, offline) },
        onFavoriteClick = viewModel::toggleSongFavorite,
        onChangeFavorite = viewModel::changeSongFavorite,
        onAddSongToPlaylists = viewModel::addSongToPlaylists,
        onAddToQueue = viewModel::addSongToQueue,
        onPlaySongInQueue = viewModel::playSongInQueue,
        onSeek = viewModel::seekPlayer,
        onSeekToPrevious = viewModel::seekPlayerToPrevious,
        onSeekToNext = viewModel::seekPlayerToNext,
        onPlayPause = viewModel::playPausePlayer,
        topBarActions = {
            IconButton(onClick = { playlistMenuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = playlistMenuExpanded,
                onDismissRequest = { playlistMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.edit)) },
                    onClick = {
                        playlistMenuExpanded = false
                        showEditPlaylistDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.delete)) },
                    onClick = {
                        playlistMenuExpanded = false
                        showDeletePlaylistDialog = true
                    }
                )
            }
        },
        failureContent = if (state.isError && !state.hasCachedFeed) { modifier ->
            RequestFailedContent(
                message = if (state.isConnectionError) connectionFailedMessage else loadFailedMessage,
                retry = { viewModel.loadPlaylist(route.id) },
                modifier = modifier,
            )
        } else null,
        extraSongMenuItems = { song, menuExpanded ->
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.remove_from_playlist)) },
                onClick = {
                    viewModel.removeSongFromPlaylist(song)
                    menuExpanded.value = false
                }
            )
        }
    )
}
