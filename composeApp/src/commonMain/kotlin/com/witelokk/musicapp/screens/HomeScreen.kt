package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.witelokk.musicapp.api.models.HomeFeedItem
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.ReleaseType
import com.witelokk.musicapp.api.models.SearchResultItem
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.components.AddToPlaylistsDialog
import com.witelokk.musicapp.components.AppSnackbarHost
import com.witelokk.musicapp.components.Avatar
import com.witelokk.musicapp.components.Card
import com.witelokk.musicapp.components.CreatePlaylistCard
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
import com.witelokk.musicapp.viewmodel.SearchViewModel
import com.witelokk.musicapp.viewmodel.SearchViewModelState
import com.witelokk.musicapp.withoutBottom
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val searchState by searchViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val loadFailedMessage = stringResource(Res.string.load_failed_message)
    val connectionFailedMessage = stringResource(Res.string.connection_failed_message)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        rememberStandardBottomSheetState(
            skipHiddenState = false,
            confirmValueChange = { value -> value != SheetValue.Hidden },
            initialValue = SheetValue.Hidden
        )
    )
    val searchExpanded = rememberSaveable { mutableStateOf(false) }
    val searchQuery = remember { MutableStateFlow("") }

    LaunchedEffect(Unit) {
        searchQuery.debounce(1.seconds).collectLatest {
            searchViewModel.search(it)
        }
    }

    var songToAddToPlaylists by remember { mutableStateOf<Song?>(null) }
    var showAddToPlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var playlistName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(showAddToPlaylistDialog) {
        if (showAddToPlaylistDialog) {
            viewModel.loadPlaylists()
        }
    }

    LaunchedEffect(state.snackbarEventId) {
        if (state.snackbarEventId == 0L || !state.hasCachedFeed) {
            return@LaunchedEffect
        }

        if (state.isError && !state.isConnectionError) {
            snackbarHostState.showSnackbar(loadFailedMessage)
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

    CreatePlaylistDialog(
        showDialog = showCreatePlaylistDialog,
        playlistName = playlistName,
        onNameChange = { playlistName = it },
        onDismissRequest = { showCreatePlaylistDialog = false },
        onCreate = {
            viewModel.createPlaylist(playlistName)
            playlistName = ""
            showCreatePlaylistDialog = false
        },
    )

    LaunchedEffect(searchExpanded.value) {
        if (!searchExpanded.value) {
            searchViewModel.clearSearchState()
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
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        HomeScreenScaffoldContent(
            innerPadding,
            navController,
            searchExpanded,
            searchQuery,
            state,
            searchState,
            viewModel,
            searchViewModel,
            onCreatePlaylistClick = { showCreatePlaylistDialog = true },
            loadFailedMessage,
            connectionFailedMessage,
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
    searchState: SearchViewModelState,
    viewModel: HomeScreenViewModel,
    searchViewModel: SearchViewModel,
    onCreatePlaylistClick: () -> Unit,
    loadFailedMessage: String,
    connectionFailedMessage: String,
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
            SearchContent(searchState, searchViewModel, viewModel, searchQuery, navController)
        }

        LoadingContainer(state.isLoading) {
            if (state.isError && !state.hasCachedFeed) {
                RequestFailedContent(
                    message = if (state.isConnectionError) connectionFailedMessage else loadFailedMessage,
                    retry = { viewModel.retryLoadHomeFeed() },
                    modifier = Modifier.fillMaxSize(),
                )
                return@LoadingContainer
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)
            ) {
                items(state.feed.sections) { section ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            section.titles[Locale.current.language] ?: section.titles["en"] ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(24.dp)
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(section.items) { item ->
                                HomeFeedItemCard(item, navController)
                            }
                            if (section.items.any { it.type == HomeFeedItem.Type.playlist || it.type == HomeFeedItem.Type.favorites }) {
                                item {
                                    CreatePlaylistCard(
                                        modifier = Modifier.clickable(onClick = onCreatePlaylistClick)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFeedItemCard(
    item: HomeFeedItem,
    navController: NavController,
) {
    when (item.type) {
        HomeFeedItem.Type.release -> item.release?.let { release ->
            Card(
                title = release.name,
                subtitle = release.artists.names,
                pictureUrl = release.coverUrl,
                fixedSize = true,
                modifier = Modifier.clickable {
                    navController.navigate(ReleaseScreenRoute(release.id))
                },
            )
        }

        HomeFeedItem.Type.playlist -> item.playlist?.let { playlist ->
            PlaylistCard(
                playlist,
                modifier = Modifier.clickable {
                    navController.navigate(PlaylistScreenRoute(playlist.id))
                },
            )
        }

        HomeFeedItem.Type.artist -> item.artist?.let { artist ->
            Card(
                title = artist.name,
                subtitle = stringResource(Res.string.artist_card_subtitle),
                pictureUrl = artist.avatarUrl,
                fixedSize = true,
                modifier = Modifier.clickable {
                    navController.navigate(ArtistScreenRoute(artist.id))
                },
            )
        }

        HomeFeedItem.Type.favorites -> FavoriteCard(
            modifier = Modifier.clickable {
                navController.navigate("favorites")
            },
        )
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
        fixedSize = true,
    )
}

@Composable
private fun SearchContent(
    state: SearchViewModelState,
    searchViewModel: SearchViewModel,
    viewModel: HomeScreenViewModel,
    searchQuery: MutableStateFlow<String>,
    navController: NavController
) {
    if (state.isLoading) {
        SearchLoadingContent()
    } else if (state.isFailure) {
        SearchFailedContent({
            searchViewModel.search(searchQuery.value)
        })
    } else if (state.results?.results?.isEmpty() == true) {
        SearchEmptyContent()
    } else if (searchQuery.collectAsState().value.isBlank()) {
        SearchHistoryContent(state.history, onResultClick = {
            when (it.type) {
                SearchResultItem.Type.song -> viewModel.setPlayerQueueAndPlay(listOf(it.song!!), 0)
                SearchResultItem.Type.release -> navController.navigate(
                    ReleaseScreenRoute(it.release!!.id)
                )

                SearchResultItem.Type.artist -> navController.navigate(ArtistScreenRoute(it.artist!!.id))
                SearchResultItem.Type.playlist -> navController.navigate(
                    PlaylistScreenRoute(it.playlist!!.id)
                )
            }
        }, onClearClick = {
            searchViewModel.clearSearchHistory()
        }, songDownloadState = viewModel::songCacheState)
    } else {
        SearchSuccessfulContent(
            state.results?.results ?: listOf(),
            onResultClick = {
                searchViewModel.addToSearchHistory(it)
                when (it.type) {
                    SearchResultItem.Type.song -> viewModel.setPlayerQueueAndPlay(listOf(it.song!!), 0)
                    SearchResultItem.Type.release -> navController.navigate(
                        ReleaseScreenRoute(it.release!!.id)
                    )

                    SearchResultItem.Type.artist -> navController.navigate(ArtistScreenRoute(it.artist!!.id))
                    SearchResultItem.Type.playlist -> navController.navigate(
                        PlaylistScreenRoute(it.playlist!!.id)
                    )
                }
            },
            songDownloadState = viewModel::songCacheState
        )
    }
}

@Composable
fun ReleaseType.toLocalizedString(): String {
    return when(this) {
        ReleaseType.single -> stringResource(Res.string.release_type_single)
        ReleaseType.album -> stringResource(Res.string.release_type_album)
        ReleaseType.ep -> stringResource(Res.string.release_type_ep)
    }
}
