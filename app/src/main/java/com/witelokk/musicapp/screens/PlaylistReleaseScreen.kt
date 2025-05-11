package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.R
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.SongListItem
import com.witelokk.musicapp.viewmodel.PlaylistReleaseScreenViewModel
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

    LaunchedEffect(Unit) {
        if (route.type == PlaylistReleaseScreenType.PLAYLIST)
            viewModel.loadPlaylist(route.id)
        else
            viewModel.loadRelease(route.id)
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
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.songs.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(stringResource(R.string.playlist_is_empty))
                }
            }
            LazyColumn {
                items(state.songs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = state.playerState?.song?.id == song.id,
                        onFavoriteClick = { viewModel.removeSongFromFavorites(song) },
                        modifier = Modifier
                            .clickable { viewModel.playSong(song) }
                            .padding(horizontal = 20.dp, vertical = 8.dp))
                }
            }
        }
    }
}