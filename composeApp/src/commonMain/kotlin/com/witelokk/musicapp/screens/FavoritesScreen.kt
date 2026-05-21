package com.witelokk.musicapp.screens

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.witelokk.musicapp.viewmodel.FavoritesScreenViewModel
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val loadFailedMessage = stringResource(Res.string.favorite_songs_load_failed)
    val connectionFailedMessage = stringResource(Res.string.favorite_songs_connection_failed)

    LaunchedEffect(state.snackbarEventId) {
        if (state.snackbarEventId == 0L) {
            return@LaunchedEffect
        }

        when {
            state.isConnectionError -> snackbarHostState.showSnackbar(connectionFailedMessage)
            state.isError -> snackbarHostState.showSnackbar(loadFailedMessage)
        }
    }

    SongListScreen(
        navController = navController,
        title = stringResource(Res.string.favorite_songs_title),
        songs = state.songs,
        playlists = state.playlists,
        playerState = state.playerState,
        isLoading = state.isLoading,
        showContent = !state.isLoading && state.hasObservedFavorites,
        emptyMessage = stringResource(Res.string.no_favorite_songs_message),
        showFavorite = false,
        songCacheState = viewModel::songCacheState,
        onLoadPlaylists = viewModel::loadPlaylists,
        onPlayAllSongs = { offline -> viewModel.playAllSongs(offline) },
        onShufflePlayAllSongs = { offline -> viewModel.shufflePlayAllSongs(offline) },
        onSongClick = { song, offline -> viewModel.playSong(song, offline) },
        onChangeFavorite = viewModel::changeSongFavorite,
        onAddSongToPlaylists = viewModel::addSongToPlaylists,
        onAddToQueue = viewModel::addSongToQueue,
        onPlaySongInQueue = viewModel::playSongInQueue,
        onSeek = viewModel::seekPlayer,
        onSeekToPrevious = viewModel::seekPlayerToPrevious,
        onSeekToNext = viewModel::seekPlayerToNext,
        onPlayPause = viewModel::playPausePlayer,
        snackbarHost = { _ -> SnackbarHost(snackbarHostState) },
        extraSongMenuItems = { song, _ ->
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.remove_from_favorite_songs_menu_item)) },
                onClick = { viewModel.changeSongFavorite(song, false) }
            )
        }
    )
}
