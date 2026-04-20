package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.api.apis.CompatAuthApi
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.api.models.ArtistList
import com.witelokk.musicapp.api.models.CreatePlaylistRequest
import com.witelokk.musicapp.api.models.HomeFeed
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.PlaylistsSummary
import com.witelokk.musicapp.api.models.SearchResponse
import com.witelokk.musicapp.api.models.SearchResultItem
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.ConnectionErrorException
import com.witelokk.musicapp.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class HomeViewModelState(
    val feed: HomeFeed = HomeFeed(
        PlaylistsSummary(0, listOf()),
        ArtistList(0, listOf(), ""),
        listOf()
    ),
    val isLoading: Boolean = true,
    val hasObservedFeed: Boolean = false,
    val hasCachedFeed: Boolean = false,
    val isConnectionError: Boolean = false,
    val isFailure: Boolean = false,
    val isError: Boolean = false,
    val snackbarEventId: Long = 0,
    val isSearchLoading: Boolean = false,
    val isSearchFailure: Boolean = false,
    val searchResults: SearchResponse? = null,
    val searchHistory: List<SearchResultItem> = listOf(),
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState? = null,
    val accountName: String = "",
)

class HomeScreenViewModel(
    private val searchApi: SearchApi,
    private val homeRepository: HomeRepository,
    private val settings: SettingsRepository,
    private val playlistsApi: PlaylistsApi,
    private val authApi: CompatAuthApi,
    favoritesApi: FavoritesApi,
    musicPlayer: MusicPlayer,
) : BaseViewModel(musicPlayer, favoritesApi, playlistsApi) {
    private val _state = MutableStateFlow(HomeViewModelState(playerState = musicPlayer.state.value))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            var isInitialEmission = true

            homeRepository.observeFeed().collect { feed ->
                _state.update {
                    it.copy(
                        isLoading = isInitialEmission && feed == null,
                        hasObservedFeed = true,
                        hasCachedFeed = feed != null || it.hasCachedFeed,
                        isConnectionError = false,
                        isError = false,
                        feed = feed ?: it.feed,
                    )
                }

                isInitialEmission = false
            }
        }

        loadHomeFeed()
        loadSearchHistory()
        loadProfile()

        viewModelScope.launch {
            musicPlayer.state.collect { newPlayerState ->
                _state.update { currentState ->
                    currentState.copy(playerState = newPlayerState)
                }
            }
        }

        viewModelScope.launch {
            settings.accountName.collect { accountName ->
                _state.update { it.copy(accountName = accountName) }
            }
        }
    }

    fun loadHomeFeed() {
        launchCatching(action = "load home page feed", onError = {
            _state.update { state ->
                state.copy(
                    isConnectionError = it is ConnectionErrorException,
                    isError = true,
                    isLoading = false,
                    snackbarEventId = state.snackbarEventId + 1,
                )
            }
        }) {
            homeRepository.refreshHomeFeed()
        }
    }

    fun retryLoadHomeFeed() {
        _state.update {
            it.copy(
                isConnectionError = false,
                isError = false,
                isLoading = !it.hasCachedFeed,
            )
        }
        loadHomeFeed()
    }

    fun dismissLoadHomePageError() {
        _state.update {
            it.copy(
                isConnectionError = false,
                isError = false,
            )
        }
    }

    fun loadSearchHistory() = viewModelScope.launch {
        settings.searchHistory.collect { history ->
            _state.update {
                it.copy(
                    searchHistory = if (history.isEmpty()) listOf() else Json.decodeFromString(
                        history
                    )
                )
            }
        }
    }

    fun loadProfile() = viewModelScope.launch {
        val response = runApiCatching(action = "load profile") {
            authApi.getCurrentUser()
        } ?: return@launch

        if (response.logIfFailure("load profile")) {
            return@launch
        }

        val me = response.body()

        settings.setAccountName(me.name)
        settings.setAccountEmail(me.email)

        _state.update {
            it.copy(
                accountName = me.name,
            )
        }
    }

    fun addToSearchHistory(result: SearchResultItem) {
        val history = _state.value.searchHistory.toMutableList()
        if (history.contains(result))
            return
        history.add(result)
        if (history.size > 10) {
            history.removeAt(0)
        }
        viewModelScope.launch {
            settings.setSearchHistory(Json.encodeToString(history))
        }
        _state.update { it.copy(searchHistory = history) }
    }

    fun search(query: String) {
        if (query.isEmpty()) {
            return
        }

        _state.update { it.copy(isSearchLoading = true) }

        launchCatching(action = "search for '$query'", onError = {
            _state.update {
                it.copy(
                    isSearchLoading = false,
                    isSearchFailure = true,
                    searchResults = null
                )
            }
        }) {
            searchApi.search(query, type = null, page = 1, limit = 10).let { response ->
                if (response.success) {
                    _state.update {
                        it.copy(
                            isSearchLoading = false,
                            isSearchFailure = false,
                            searchResults = response.body()
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isSearchLoading = false,
                            isSearchFailure = true,
                            searchResults = null
                        )
                    }
                }
            }
        }
    }

    fun clearSearchHistory() = viewModelScope.launch {
        settings.setSearchHistory("[]")
        _state.update {
            it.copy(searchHistory = listOf())
        }
    }

    fun clearSearchState() {
        _state.update {
            it.copy(
                isSearchLoading = false,
                isSearchFailure = false,
                searchResults = null
            )
        }
    }

    fun loadPlaylists() {
        launchCatching(action = "load playlists for home screen") {
            val response = playlistsApi.getPlaylists()

            if (response.logIfFailure("load playlists for home screen")) {
                return@launchCatching
            }

            _state.update {
                it.copy(playlists = response.body().playlists)
            }
        }
    }

    fun createPlaylist(name: String) {
        launchCatching(action = "create playlist '$name'") {
            val response = playlistsApi.createPlaylist(CreatePlaylistRequest(name))

            if (response.logIfFailure("create playlist '$name'")) {
                return@launchCatching
            }

            val newPlaylist = PlaylistSummary(
                id = response.body().id,
                name = name,
                songsCount = 0
            )

            _state.update {
                it.copy(
                    playlists = listOf(newPlaylist) + it.playlists,
                    feed = it.feed.copy(
                        playlists = it.feed.playlists.copy(
                            count = it.feed.playlists.count + 1,
                            listOf(newPlaylist) + it.feed.playlists.playlists
                        )
                    )
                )
            }
        }
    }
}
