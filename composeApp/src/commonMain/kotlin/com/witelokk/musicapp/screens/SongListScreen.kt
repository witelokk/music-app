package com.witelokk.musicapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCacheState
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.SongCollectionList
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.rememberHttpConnectivityState
import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.navigate_back_content_description
import musicapp.composeapp.generated.resources.play_content_description
import musicapp.composeapp.generated.resources.playlist_empty_message
import musicapp.composeapp.generated.resources.shuffle_play_content_description
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongListScreen(
    navController: NavController,
    title: String,
    songs: List<Song>,
    playlists: List<PlaylistSummary>,
    playerState: PlayerState?,
    isLoading: Boolean,
    showContent: Boolean,
    emptyMessage: String = stringResource(Res.string.playlist_empty_message),
    showFavorite: Boolean = true,
    showPlayAllAction: Boolean = true,
    showAddToQueueMenuItem: Boolean = true,
    topBarActions: @Composable () -> Unit = {},
    failureContent: @Composable ((Modifier) -> Unit)? = null,
    songCacheState: (Song) -> StateFlow<MediaCacheState>,
    onLoadPlaylists: () -> Unit,
    onPlayAllSongs: (Boolean) -> Unit,
    onShufflePlayAllSongs: (Boolean) -> Unit,
    onSongClick: (Song, Boolean) -> Unit,
    onFavoriteClick: (Song) -> Unit = {},
    onChangeFavorite: (Song, Boolean) -> Unit,
    onAddSongToPlaylists: (Song, List<String>) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlaySongInQueue: (Int) -> Unit,
    onSeek: (kotlin.time.Duration) -> Unit,
    onSeekToPrevious: () -> Unit,
    onSeekToNext: () -> Unit,
    onPlayPause: () -> Unit,
    extraSongMenuItems: @Composable (song: Song, menuExpanded: MutableState<Boolean>) -> Unit = { _, _ -> },
    snackbarHost: @Composable (androidx.compose.material3.SnackbarHostState) -> Unit = {
        androidx.compose.material3.SnackbarHost(it)
    },
) {
    val scope = rememberCoroutineScope()
    val connectivityState = rememberHttpConnectivityState()
    val isOffline = connectivityState.status !is Connectivity.Status.Connected

    var addToPlaylistDialogSong by rememberSaveable { mutableStateOf<Song?>(null) }

    LaunchedEffect(addToPlaylistDialogSong) {
        if (addToPlaylistDialogSong != null) {
            onLoadPlaylists()
        }
    }

    var showLoadingIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(1000)
            showLoadingIndicator = true
        } else {
            showLoadingIndicator = false
        }
    }

    AddToPlaylistsDialog(
        showDialog = addToPlaylistDialogSong != null,
        playlists = playlists,
        onDismissRequest = { addToPlaylistDialogSong = null },
        onAddRequest = { selectedPlaylists ->
            onAddSongToPlaylists(addToPlaylistDialogSong!!, selectedPlaylists)
            addToPlaylistDialogSong = null
        },
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    PlayerSheetScaffold(
        navController,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(Res.string.navigate_back_content_description))
                    }
                },
                actions = {
                    if (showPlayAllAction) {
                        IconButton(onClick = { onShufflePlayAllSongs(isOffline) }) {
                            Icon(Icons.Outlined.Shuffle, stringResource(Res.string.shuffle_play_content_description))
                        }
                        IconButton(onClick = { onPlayAllSongs(isOffline) }) {
                            Icon(Icons.Outlined.PlayArrow, stringResource(Res.string.play_content_description))
                        }
                    }
                    topBarActions()
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        playerState = playerState,
        onSeek = onSeek,
        onSeekToPrevious = onSeekToPrevious,
        onSeekToNext = onSeekToNext,
        onPlayPause = onPlayPause,
        onAddToPlaylist = { song ->
            addToPlaylistDialogSong = song
        },
        onChangeFavorite = onChangeFavorite,
        onPlaySongInQueue = onPlaySongInQueue,
        snackbarHost = snackbarHost,
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
        } else if (failureContent != null) {
            failureContent(Modifier.padding(innerPadding))
        } else {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SongCollectionList(
                    songs = songs,
                    playerState = playerState,
                    connectivityStatus = connectivityState.status,
                    emptyMessage = emptyMessage,
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                    ),
                    showFavorite = showFavorite,
                    showAddToQueueMenuItem = showAddToQueueMenuItem,
                    songCacheState = songCacheState,
                    onSongClick = { song -> onSongClick(song, isOffline) },
                    onFavoriteClick = onFavoriteClick,
                    onAddToPlaylist = { song ->
                        scope.launch {
                            delay(100)
                            addToPlaylistDialogSong = song
                        }
                    },
                    onAddToQueue = onAddToQueue,
                    extraMenuItems = extraSongMenuItems
                )
            }
        }
    }
}
