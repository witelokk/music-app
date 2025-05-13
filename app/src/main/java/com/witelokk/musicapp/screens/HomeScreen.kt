package com.witelokk.musicapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.witelokk.musicapp.R
import com.witelokk.musicapp.components.AddCard
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.FavoriteCard
import com.witelokk.musicapp.components.EntityCard
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.Search
import com.witelokk.musicapp.components.SearchContent
import com.witelokk.musicapp.components.SearchEmptyContent
import com.witelokk.musicapp.components.SearchFailedContent
import com.witelokk.musicapp.components.SearchHistoryContent
import com.witelokk.musicapp.components.SearchLoadingContent
import com.witelokk.musicapp.data.Entity
import com.witelokk.musicapp.withoutBottom
import com.witelokk.musicapp.viewmodel.HomeScreenViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = koinViewModel()
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

    val scaffoldState = rememberBottomSheetScaffoldState(
        rememberStandardBottomSheetState(
            skipHiddenState = false,
            confirmValueChange = { value -> value != SheetValue.Hidden },
            initialValue = SheetValue.Hidden
        )
    )
    val searchExpanded = rememberSaveable { mutableStateOf(false) }
    var searchQuery = remember { MutableStateFlow("") }

    LaunchedEffect(Unit) {
        viewModel.viewModelScope.launch {
            searchQuery.debounce(2.seconds).collectLatest {
                viewModel.search(it)
            }
        }
    }

    var songIdToAddToPlaylists by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddToPlaylistDialog by rememberSaveable { mutableStateOf(false) }

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
                    songIdToAddToPlaylists!!,
                    playlists
                ); showAddToPlaylistDialog = false
            },
        )
    }

    LaunchedEffect(searchExpanded.value) {
        if (!searchExpanded.value) {
            viewModel.clearSearchState()
            if (state.playerState != null)
                scaffoldState.bottomSheetState.partialExpand()
        } else {
            scaffoldState.bottomSheetState.hide()
        }
    }

    PlayerSheetScaffold(
        navController,
        playerState = state.playerState,
        scaffoldState = scaffoldState,
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
        Column(modifier = Modifier.padding(innerPadding.withoutBottom())) {
            Search(
                navController,
                expanded = searchExpanded,
                onQueryChanged = {
                    searchQuery.value = it
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSearchLoading) {
                    SearchLoadingContent()
                } else if (state.isSearchFailure) {
                    SearchFailedContent({
                        viewModel.search(searchQuery.value)
                    })
                } else if (state.searchResults?.results?.isEmpty() == true) {
                    SearchEmptyContent()
                } else if (searchQuery.collectAsState().value.isBlank()) {
                    SearchHistoryContent(state.searchHistory, onResultClick = {
                        when (it.type) {
                            "song" -> viewModel.setPlayerQueueAndPlay(listOf(it.song!!), 0)
                            "release" -> navController.navigate(
                                PlaylistReleaseScreenRoute(
                                    PlaylistReleaseScreenType.RELEASE,
                                    it.release!!.id
                                )
                            )

                            "artist" -> navController.navigate(ArtistScreenRoute(it.artist!!.id))
                            "playlist" -> navController.navigate(
                                PlaylistReleaseScreenRoute(
                                    PlaylistReleaseScreenType.PLAYLIST,
                                    it.playlist!!.id
                                )
                            )
                        }
                    }, onClearClick = {
                        viewModel.clearSearchHistory()
                    })
                } else {
                    SearchContent(state.searchResults?.results ?: listOf(), onResultClick = {
                        viewModel.addToSearchHistory(it)
                        when (it.type) {
                            "song" -> viewModel.setPlayerQueueAndPlay(listOf(it.song!!), 0)
                            "release" -> navController.navigate(
                                PlaylistReleaseScreenRoute(
                                    PlaylistReleaseScreenType.RELEASE,
                                    it.release!!.id
                                )
                            )

                            "artist" -> navController.navigate(ArtistScreenRoute(it.artist!!.id))
                            "playlist" -> navController.navigate(
                                PlaylistReleaseScreenRoute(
                                    PlaylistReleaseScreenType.PLAYLIST,
                                    it.playlist!!.id
                                )
                            )
                        }
                    })
                }
            }
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
                androidx.compose.animation.AnimatedVisibility(
                    visible = !state.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn {
                        item {
                            Text(
                                stringResource(R.string.playlists),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(24.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    FavoriteCard(modifier = Modifier.clickable {
                                        navController.navigate(
                                            "favorites"
                                        )
                                    })
                                }
                                items(state.layout.playlists.playlists) { playlist ->
                                    EntityCard(Entity(
                                        playlist.name,
                                        stringResource(R.string.playlist),
                                        playlist.coverUrl
                                    ),
                                        modifier = Modifier.clickable {
                                            navController.navigate(
                                                PlaylistReleaseScreenRoute(
                                                    PlaylistReleaseScreenType.PLAYLIST,
                                                    playlist.id
                                                )
                                            )
                                        })
                                }
                                item {
                                    AddCard(modifier = Modifier.clickable { navController.navigate("playlist") })
                                }
                            }
                        }
                        items(state.layout.sections) { section ->
                            Text(
                                section.title,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(24.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(section.releases.releases) { release ->
                                    EntityCard(Entity(
                                        release.name,
                                        release.artists.names,
                                        release.coverUrl
                                    ),
                                        modifier = Modifier
                                            .width(155.dp)
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
                        item {
                            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 24.dp))
                        }
                    }
                }
            }
        }
    }
}