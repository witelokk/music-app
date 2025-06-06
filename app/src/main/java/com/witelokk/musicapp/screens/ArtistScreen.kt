package com.witelokk.musicapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R
import com.witelokk.musicapp.add
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.Card
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.SongListItem
import com.witelokk.musicapp.viewmodel.ArtistScreenViewModel
import com.witelokk.musicapp.withoutBottom
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data class ArtistScreenRoute(
    val id: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    artist: ArtistScreenRoute,
    viewModel: ArtistScreenViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    var showLoadingIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            delay(1000) // delay for 1 second
            showLoadingIndicator = true
        } else {
            showLoadingIndicator = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadArtist(artist.id)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showAddToPlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var songToAddToPlaylists by remember { mutableStateOf<Song?>(null) }
    var albumsFilter by rememberSaveable { mutableStateOf(false) }
    var singlesEPFilter by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showAddToPlaylistDialog) {
        if (showAddToPlaylistDialog) {
            viewModel.loadPlaylists()
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

    PlayerSheetScaffold(
        navController,
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
        topBar = {
            TopAppBar({
                AnimatedVisibility(
                    visible = !state.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Text(state.artist?.name ?: "")
                    }
                }
            }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back)
                    )
                }
            }, actions = {
                IconButton(onClick = { viewModel.toggleFollowing() }) {
                    Icon(
                        if (state.artist?.following == true) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (state.artist?.following == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = {
                    viewModel.playAllSongs()
                }) {
                    Icon(Icons.Outlined.PlayArrow, stringResource(R.string.play))
                }
            }, scrollBehavior = scrollBehavior)
        }) { innerPadding ->
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp).add(bottom = 8.dp + innerPadding.calculateBottomPadding()),
                    modifier = Modifier
                        .padding(innerPadding.withoutBottom())
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            AsyncImage(
                                state.artist?.coverUrl ?: "", null, modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(16.dp)
                                    )
                                    .aspectRatio(21f / 9)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                stringResource(R.string.popular),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                            )
                        }
                    }

                    items(
                        state.artist?.popularSongs?.songs ?: listOf(),
                        span = { GridItemSpan(2) }) { song ->
                        SongListItem(
                            song = song,
                            showDuration = true,
                            modifier = Modifier
                                .clickable {
                                    viewModel.playPopularSong(song)
                                }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            onFavoriteClick = {
                                viewModel.changeSongFavorite(song, !song.isFavorite)
                            },
                            isActive = (song.id == state.playerState?.currentSong?.id),
                            isPlaying = state.playerState?.playing ?: false,
                        ) { menuExpanded ->
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

                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Text(
                                stringResource(R.string.releases),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(albumsFilter,
                                    onClick = {
                                        albumsFilter = !albumsFilter;
                                        singlesEPFilter = false;
                                        if (albumsFilter) {
                                            viewModel.filterReleases("album")
                                        }
                                        if (!singlesEPFilter && !albumsFilter) {
                                            viewModel.filterReleases(null)
                                        }
                                    },
                                    { Text(stringResource(R.string.albums)) })
                                FilterChip(singlesEPFilter,
                                    onClick = {
                                        singlesEPFilter = !singlesEPFilter; albumsFilter = false;
                                        if (singlesEPFilter) {
                                            viewModel.filterReleases("single")
                                        }
                                        if (!singlesEPFilter && !albumsFilter) {
                                            viewModel.filterReleases(null)
                                        }
                                    },
                                    { Text(stringResource(R.string.singles_and_eps)) })
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    items(
                        state.filteredArtist?.releases?.releases ?: listOf(),
                        key = { it.id }) { release ->
                        Card(
                            title = release.name,
                            subtitle = when (release.type) {
                                "single" -> stringResource(R.string.single)
                                "album" -> stringResource(R.string.album)
                                else -> release.type
                            } + ", " + release.releasedAt.substring(0, 4),
                            pictureUrl = release.coverUrl,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .animateItem()
                                .clickable {
                                    navController.navigate(
                                        PlaylistReleaseScreenRoute(
                                            PlaylistReleaseScreenType.RELEASE,
                                            release.id
                                        )
                                    )
                                })
                    }
                }
            }
        }
    }
}
