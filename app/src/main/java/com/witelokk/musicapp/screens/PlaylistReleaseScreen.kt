package com.witelokk.musicapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.R
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.SongListItem
import com.witelokk.musicapp.viewmodel.PlaylistReleaseScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
enum class PlaylistReleaseScreenType {
    PLAYLIST, RELEASE
}

@Serializable
data class PlaylistReleaseScreenRoute(
    val type: PlaylistReleaseScreenType,
    val id: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistReleaseScreen(
    navController: NavController,
    route: PlaylistReleaseScreenRoute,
    viewModel: PlaylistReleaseScreenViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    var songToAddToPlaylists by rememberSaveable { mutableStateOf<Song?>(null) }
    var showAddToPlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showDeletePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showEditPlaylistDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (route.type == PlaylistReleaseScreenType.PLAYLIST)
            viewModel.loadPlaylist(route.id)
        else
            viewModel.loadRelease(route.id)
    }

    LaunchedEffect(showAddToPlaylistDialog) {
        if (showAddToPlaylistDialog) {
            viewModel.loadPlaylists()
        }
    }

    var showLoadingIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            delay(1000) // delay for 1 second
            showLoadingIndicator = true
        } else {
            showLoadingIndicator = false
        }
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistsDialog(
            state.playlists,
            onDismissRequest = { showAddToPlaylistDialog = false },
            onAddRequest = { playlists ->
                viewModel.addSongToPlaylists(
                    songToAddToPlaylists!!,
                    playlists
                ); showAddToPlaylistDialog = false
            },
        )
    }

    if (showDeletePlaylistDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.delete_playlists_dialog_title)) },
            onDismissRequest = { showDeletePlaylistDialog = false },
            confirmButton = {
                TextButton(onClick = { showDeletePlaylistDialog = false }) { Text(stringResource(R.string.cancel)) }
                TextButton(onClick = { viewModel.deletePlaylist() }) { Text(stringResource(R.string.yes)) }
            },
            text = { Text(stringResource(R.string.delete_playlists_dialog_text)) }
        )
    }

    if (showEditPlaylistDialog) {
        var newName by remember { mutableStateOf(state.name) }

        AlertDialog(
            title = { Text(stringResource(R.string.edit_playlist_name_dialog_title)) },
            onDismissRequest = { showDeletePlaylistDialog = false },
            confirmButton = {
                TextButton(onClick = { showEditPlaylistDialog = false }) { Text(stringResource(R.string.cancel)) }
                TextButton(onClick = {
                    viewModel.editPlaylistName(newName)
                    showEditPlaylistDialog = false
                }) { Text(stringResource(R.string.yes)) }
            },
            text = {
                OutlinedTextField(newName, onValueChange = { newName = it })
            }
        )
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            navController.navigateUp()
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    PlayerSheetScaffold(
        navController,
        topBar = {
            TopAppBar(
                title = { Text(state.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    if (route.type == PlaylistReleaseScreenType.PLAYLIST) {
                        IconButton(onClick = { showEditPlaylistDialog = true }) {
                            Icon(Icons.Outlined.Edit, stringResource(R.string.edit))
                        }
                        IconButton(onClick = { showDeletePlaylistDialog = true }) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.delete))
                        }
                    }
                    IconButton(onClick = { viewModel.playAllSongs() }) {
                        Icon(Icons.Outlined.PlayArrow, stringResource(R.string.play))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        playerState = state.playerState,
        onSeek = { viewModel.seekPlayer(it) },
        onSeekToPrevious = { viewModel.seekPlayerToPrevious() },
        onSeekToNext = { viewModel.seekPlayerToNext() },
        onPlayPause = { viewModel.playPausePlayer() },
        onAddToPlaylist = { song ->
            songToAddToPlaylists = song
            showAddToPlaylistDialog = true
        },
        onChangeFavorite = { song, favorite ->
            viewModel.changeSongFavorite(song, favorite)
        },
        onPlaySongInQueue = { index -> viewModel.playSongInQueue(index) },
    ) { innerPadding ->
        if (showLoadingIndicator) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AnimatedVisibility(
                visible = !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (state.songs.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(stringResource(R.string.playlist_is_empty))
                    }
                }
                LazyColumn(contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)) {
                    items(state.songs) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = state.playerState?.currentSong?.id == song.id,
                            onFavoriteClick = { viewModel.toggleSongFavorite(song) },
                            modifier = Modifier
                                .clickable { viewModel.playSong(song) }
                                .padding(horizontal = 20.dp, vertical = 8.dp)) { menuExpanded ->
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_to_playlist)) },
                                onClick = {
                                    menuExpanded.value = false
                                    songToAddToPlaylists = song
                                    showAddToPlaylistDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_to_queue)) },
                                onClick = {
                                    viewModel.addSongToQueue(song)
                                    menuExpanded.value = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}