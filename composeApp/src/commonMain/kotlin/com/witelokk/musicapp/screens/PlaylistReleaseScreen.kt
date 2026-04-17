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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.DeletePlaylistDialog
import com.witelokk.musicapp.components.EditPlaylistNameDialog
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.RequestFailedContent
import com.witelokk.musicapp.components.SongListItem
import com.witelokk.musicapp.viewmodel.PlaylistReleaseScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Serializable
data class PlaylistReleaseScreenRoute(
    val type: String,
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

    val scope = rememberCoroutineScope()

    var addToPlaylistDialogSong by rememberSaveable { mutableStateOf<Song?>(null) }
    var showDeletePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showEditPlaylistDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (route.type == "playlist")
            viewModel.loadPlaylist(route.id)
        else
            viewModel.loadRelease(route.id)
    }

    LaunchedEffect(addToPlaylistDialogSong) {
        if (addToPlaylistDialogSong != null) {
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

    AddToPlaylistsDialog(
        showDialog = addToPlaylistDialogSong != null,
        playlists = state.playlists,
        onDismissRequest = { addToPlaylistDialogSong = null },
        onAddRequest = { playlists ->
            viewModel.addSongToPlaylists(
                addToPlaylistDialogSong!!,
                playlists
            )
            addToPlaylistDialogSong = null
        },
    )

    DeletePlaylistDialog(
        showDialog = showDeletePlaylistDialog,
        playlistName = state.name,
        onDismissRequest = { showDeletePlaylistDialog = false },
        onConfirmDelete = {
            viewModel.deletePlaylist()
            showDeletePlaylistDialog = false
        },
    )

    var newName by remember { mutableStateOf(state.name) }

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
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (route.type == "playlist") {
                        IconButton(onClick = { showEditPlaylistDialog = true }) {
                            Icon(Icons.Outlined.Edit, stringResource(Res.string.edit))
                        }
                        IconButton(onClick = { showDeletePlaylistDialog = true }) {
                            Icon(Icons.Outlined.Delete, stringResource(Res.string.delete))
                        }
                    }
                    IconButton(onClick = { viewModel.playAllSongs() }) {
                        Icon(Icons.Outlined.PlayArrow, stringResource(Res.string.play))
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
            addToPlaylistDialogSong = song
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
        } else if (state.isError) {
            RequestFailedContent(
                retry = {
                    if (route.type == "playlist") viewModel.loadPlaylist(route.id)
                    else viewModel.loadRelease(route.id)
                },
                modifier = Modifier.padding(innerPadding),
            )
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
                        Text(stringResource(Res.string.playlist_is_empty))
                    }
                }
                LazyColumn(contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)) {
                    items(state.songs) { song ->
                        SongListItem(
                            song = song,
                            isActive = state.playerState?.currentSong?.id == song.id,
                            isPlaying = state.playerState?.playing ?: false,
                            onFavoriteClick = { viewModel.toggleSongFavorite(song) },
                            modifier = Modifier
                                .clickable { viewModel.playSong(song) }
                                .padding(horizontal = 20.dp, vertical = 8.dp)) { menuExpanded ->
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.add_to_playlist)) },
                                onClick = {
                                    menuExpanded.value = false
                                    scope.launch {
                                        // wait until menu closes
                                        delay(100)
                                        addToPlaylistDialogSong = song
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.add_to_queue)) },
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
