package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.components.CreatePlaylistCard
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.Avatar
import com.witelokk.musicapp.components.Card
import com.witelokk.musicapp.components.CreatePlaylistDialog
import com.witelokk.musicapp.components.FavoriteCard
import com.witelokk.musicapp.components.LoadingContainer
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.RequestFailedContent
import com.witelokk.musicapp.components.Search
import com.witelokk.musicapp.components.SearchSuccessfulContent
import com.witelokk.musicapp.components.SearchEmptyContent
import com.witelokk.musicapp.components.SearchFailedContent
import com.witelokk.musicapp.components.SearchHistoryContent
import com.witelokk.musicapp.components.SearchLoadingContent
import com.witelokk.musicapp.viewmodel.HomeScreenViewModel
import com.witelokk.musicapp.viewmodel.HomeViewModelState
import com.witelokk.musicapp.withoutBottom
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val lifecycleOwner = navBackStackEntry?.lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadHomePageLayout()
                viewModel.loadPlaylists()
            }
        }
        lifecycleOwner?.addObserver(observer)
        onDispose { lifecycleOwner?.removeObserver(observer) }
    }

    var showCreatePlaylistDialog = remember { mutableStateOf(false) }

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

    var songToAddToPlaylists by remember { mutableStateOf<Song?>(null) }
    var showAddToPlaylistDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showAddToPlaylistDialog) {
        if (showAddToPlaylistDialog) {
            viewModel.loadPlaylists()
        }
    }

    AddToPlaylistsDialog(
        showDialog = showAddToPlaylistDialog,
        playlists = state.playlists,
        onDismissRequest = { showAddToPlaylistDialog = false },
        onAddRequest = { playlists ->
            viewModel.addSongToPlaylists(
                songToAddToPlaylists!!,
                playlists
            )
            showAddToPlaylistDialog = false
        },
    )

    var playlistName by rememberSaveable { mutableStateOf("") }

    CreatePlaylistDialog(
        showDialog = showCreatePlaylistDialog.value,
        playlistName = playlistName,
        onNameChange = { playlistName = it },
        onDismissRequest = { showCreatePlaylistDialog.value = false },
        onCreate = {
            viewModel.createPlaylist(playlistName)
            playlistName = ""
            showCreatePlaylistDialog.value = false
        },
    )

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
            songToAddToPlaylists = song
            showAddToPlaylistDialog = true
        },
        onChangeFavorite = { song, favorite ->
            viewModel.changeSongFavorite(song, favorite)
        },
        onPlaySongInQueue = { index -> viewModel.playSongInQueue(index) },
    ) { innerPadding ->
        HomeScreenScaffoldContent(
            innerPadding,
            navController,
            searchExpanded,
            searchQuery,
            state,
            viewModel,
            showCreatePlaylistDialog
        )
    }
}

@Composable
private fun HomeScreenScaffoldContent(
    innerPadding: PaddingValues,
    navController: NavController,
    searchExpanded: MutableState<Boolean>,
    searchQuery: MutableStateFlow<String>,
    state: HomeViewModelState,
    viewModel: HomeScreenViewModel,
    showCreatePlaylistDialog: MutableState<Boolean>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding.withoutBottom())
    ) {
        Search(
            navController,
            expanded = searchExpanded,
            onQueryChanged = {
                searchQuery.value = it
            },
            avatar = {
                Avatar(
                    if (state.accountName.isEmpty()) "" else state.accountName.substring(
                        0,
                        1
                    ),
                    modifier = Modifier.clickable { navController.navigate("settings") })
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            SearchContent(state, viewModel, searchQuery, navController)
        }

        LoadingContainer(state.isLoading) {
            if (state.isError) {
                RequestFailedContent(
                    retry = { viewModel.loadHomePageLayout() },
                    modifier = Modifier.fillMaxSize(),
                )
                return@LoadingContainer
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(Res.string.playlists),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(24.dp)
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
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
                                PlaylistCard(playlist, modifier = Modifier.clickable {
                                    navController.navigate(
                                        PlaylistReleaseScreenRoute(
                                            "playlist",
                                            playlist.id
                                        )
                                    )
                                })
                            }
                            item {
                                CreatePlaylistCard(
                                    modifier = Modifier.clickable {
                                        showCreatePlaylistDialog.value = true
                                    }
                                )
                            }
                        }
                    }
                }
                if (state.layout.followedArtists.count != 0) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                stringResource(Res.string.followed_artists),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(24.dp)
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.layout.followedArtists.artists) { artist ->
                                    Card(
                                        title = artist.name,
                                        subtitle = stringResource(Res.string.artist),
                                        pictureUrl = artist.avatarUrl,
                                        modifier = Modifier.clickable {
                                            navController.navigate(
                                                ArtistScreenRoute(artist.id)
                                            )
                                        })
                                }
                            }
                        }
                    }
                }
                items(state.layout.sections) { section ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            section.titleFor(Locale.current.language),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(24.dp)
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(section.releases.releases) { release ->
                                Card(
                                    title = release.name,
                                    subtitle = release.artists.names.ifBlank { release.type },
                                    pictureUrl = release.coverUrl,
                                    modifier = Modifier
                                        .width(155.dp)
                                        .clickable {
                                            navController.navigate(
                                                PlaylistReleaseScreenRoute(
                                                    "release",
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
    }
}

@Composable
private fun PlaylistCard(
    playlist: PlaylistSummary,
    modifier: Modifier,
) {
    Card(
        title = playlist.name,
        pictureUrl = playlist.coverUrl,
        modifier = modifier,
    )
}

@Composable
private fun SearchContent(
    state: HomeViewModelState,
    viewModel: HomeScreenViewModel,
    searchQuery: MutableStateFlow<String>,
    navController: NavController
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
                        "release",
                        it.release!!.id
                    )
                )

                "artist" -> navController.navigate(ArtistScreenRoute(it.artist!!.id))
                "playlist" -> navController.navigate(
                    PlaylistReleaseScreenRoute(
                        "playlist",
                        it.playlist!!.id
                    )
                )
            }
        }, onClearClick = {
            viewModel.clearSearchHistory()
        })
    } else {
        SearchSuccessfulContent(state.searchResults?.results ?: listOf(), onResultClick = {
            viewModel.addToSearchHistory(it)
            when (it.type) {
                "song" -> viewModel.setPlayerQueueAndPlay(listOf(it.song!!), 0)
                "release" -> navController.navigate(
                    PlaylistReleaseScreenRoute(
                        "release",
                        it.release!!.id
                    )
                )

                "artist" -> navController.navigate(ArtistScreenRoute(it.artist!!.id))
                "playlist" -> navController.navigate(
                    PlaylistReleaseScreenRoute(
                        "playlist",
                        it.playlist!!.id
                    )
                )
            }
        })
    }
}
