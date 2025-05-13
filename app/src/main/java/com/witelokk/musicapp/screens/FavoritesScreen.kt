package com.witelokk.musicapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.R
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.SongListItem
import com.witelokk.musicapp.viewmodel.FavoritesScreenViewModel
import com.witelokk.musicapp.withoutBottom
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    var songIdToAddToPlaylists by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddToPlaylistDialog by rememberSaveable { mutableStateOf(false) }

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
                    songIdToAddToPlaylists!!,
                    playlists
                ); showAddToPlaylistDialog = false
            },
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    PlayerSheetScaffold(
        navController,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.favorite_songs)) }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                }
            }, actions = {
                IconButton(onClick = { viewModel.playAllSongs() }) {
                    Icon(Icons.Outlined.PlayArrow, stringResource(R.string.play))
                }
            }, scrollBehavior = scrollBehavior)
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        playerState = state.playerState,
        onSeek = { viewModel.seekPlayer(it) },
        onSeekToPrevious = { viewModel.seekPlayerToPrevious() },
        onSeekToNext = { viewModel.seekPlayerToNext() },
        onPlayPause = { viewModel.playPausePlayer() },
        onAddToPlaylist = { song ->
            songIdToAddToPlaylists = song.id
            showAddToPlaylistDialog = true
        },
        onChangeFavorite = { song, favorite ->
            viewModel.changeSongFavorite(song, favorite)
        },
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
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_favorite_songs))
                    }
                }
                LazyColumn(contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)) {
                    items(state.songs) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = state.playerState?.song?.id == song.id,
                            showFavorite = false,
                            modifier = Modifier
                                .clickable { viewModel.playSong(song) }
                                .padding(horizontal = 20.dp, vertical = 8.dp)) { menuExpanded ->
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove_from_favorite_songs)) },
                                onClick = {
                                    viewModel.removeSongFromFavorites(song)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_to_playlist)) },
                                onClick = {
                                    menuExpanded.value = false
                                    songIdToAddToPlaylists = song.id
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