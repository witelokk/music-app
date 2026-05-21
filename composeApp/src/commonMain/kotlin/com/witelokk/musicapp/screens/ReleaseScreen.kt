package com.witelokk.musicapp.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.witelokk.musicapp.components.RequestFailedContent
import kotlinx.serialization.Serializable
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.connection_failed
import musicapp.composeapp.generated.resources.load_failed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.witelokk.musicapp.viewmodel.ReleaseScreenViewModel

@Serializable
data class ReleaseScreenRoute(
    val id: String
)

@Composable
fun ReleaseScreen(
    navController: NavController,
    route: ReleaseScreenRoute,
    viewModel: ReleaseScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val loadFailedMessage = stringResource(Res.string.load_failed)
    val connectionFailedMessage = stringResource(Res.string.connection_failed)

    LaunchedEffect(Unit) {
        viewModel.loadRelease(route.id)
    }

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
        onPlayAllSongs = viewModel::playAllSongs,
        onSongClick = viewModel::playSong,
        onFavoriteClick = viewModel::toggleSongFavorite,
        onChangeFavorite = viewModel::changeSongFavorite,
        onAddSongToPlaylists = viewModel::addSongToPlaylists,
        onAddToQueue = viewModel::addSongToQueue,
        onPlaySongInQueue = viewModel::playSongInQueue,
        onSeek = viewModel::seekPlayer,
        onSeekToPrevious = viewModel::seekPlayerToPrevious,
        onSeekToNext = viewModel::seekPlayerToNext,
        onPlayPause = viewModel::playPausePlayer,
        failureContent = if (state.isError && !state.hasCachedFeed) { modifier ->
            RequestFailedContent(
                message = if (state.isConnectionError) connectionFailedMessage else loadFailedMessage,
                retry = { viewModel.loadRelease(route.id) },
                modifier = modifier,
            )
        } else null,
    )
}
