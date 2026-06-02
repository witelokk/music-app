package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.CompatAuthApi
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.ArtistList
import com.witelokk.musicapp.api.models.CreatePlaylistRequest
import com.witelokk.musicapp.api.models.HomeFeed
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.PlaylistsSummary
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.ConnectionErrorException
import com.witelokk.musicapp.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState? = null,
    val accountName: String = "",
)

class HomeScreenViewModel(
    private val homeRepository: HomeRepository,
    private val settings: SettingsRepository,
    private val playlistsApi: PlaylistsApi,
    private val authApi: CompatAuthApi,
    favoritesApi: FavoritesApi,
    musicPlayer: MusicPlayer,
    private val mediaCache: MediaCache,
) : BaseViewModel(musicPlayer, favoritesApi, playlistsApi, mediaCache) {
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
