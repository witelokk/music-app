package com.witelokk.musicapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.witelokk.musicapp.components.BottomSheetMenuItem
import com.witelokk.musicapp.viewmodel.QueueScreenViewModel
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QueueScreen(navController: NavController, viewModel: QueueScreenViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    SongListScreen(
        navController,
        title = stringResource(Res.string.queue_screen_title),
        songs = state.songs,
        playlists = state.playlists,
        playerState = state.playerState,
        isLoading = state.isLoading,
        showContent = true,
        emptyMessage = stringResource(Res.string.queue_empty_message),
        showPlayAllAction = false,
        showAddToQueueMenuItem = false,
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
        extraSongMenuItems = { song, menuExpanded ->
            val index = state.songs.indexOfFirst { it.id == song.id }
            BottomSheetMenuItem(
                text = { Text(stringResource(Res.string.remove_from_queue_menu_item)) },
                onClick = {
                    if (index != -1) {
                        viewModel.removeSongFromQueue(index)
                    }
                    menuExpanded.value = false
                }
            )
        }
    )
}
