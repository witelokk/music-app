package com.witelokk.musicapp.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.BaseViewModel
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.HomeScreenApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.api.models.ArtistsSummary
import com.witelokk.musicapp.api.models.CreatePlaylistRequest
import com.witelokk.musicapp.api.models.HomeScreenLayout
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.PlaylistsSummary
import com.witelokk.musicapp.api.models.SearchResult
import com.witelokk.musicapp.api.models.SearchResultItem
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.UnknownHostException

data class HomeViewModelState(
    val layout: HomeScreenLayout = HomeScreenLayout(PlaylistsSummary(0, listOf()), ArtistsSummary(0, listOf(), ""), listOf()),
    val isLoading: Boolean = true,
    val isFailure: Boolean = false,
    val isError: Boolean = false,
    val isSearchLoading: Boolean = false,
    val isSearchFailure: Boolean = false,
    val searchResults: SearchResult? = null,
    val searchHistory: List<SearchResultItem> = listOf(),
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState? = null,
)

class HomeScreenViewModel(
    private val searchApi: SearchApi,
    private val homeScreenApi: HomeScreenApi,
    private val sharedPreferences: SharedPreferences,
    private val json: Json,
    private val playlistsApi: PlaylistsApi,
    musicPlayer: MusicPlayer,
) : BaseViewModel(musicPlayer, playlistsApi) {
    private val _state = MutableStateFlow(HomeViewModelState(playerState=musicPlayer.state.value))
    val state = _state.asStateFlow()

    init {
        loadHomePageLayout()
        loadSearchHistory()

        viewModelScope.launch {
            musicPlayer.state.collect { newPlayerState ->
                _state.update { currentState ->
                    currentState.copy(playerState = newPlayerState)
                }
            }
        }
    }

    fun loadHomePageLayout() {
        viewModelScope.launch {
            val response = homeScreenApi.homeScreenLayoutGet()

            if (!response.success) {
                _state.update { it.copy(isError = true, isLoading = false) }
                return@launch
            }

            val layout = response.body()

            _state.update {
                it.copy(
                    isLoading = false,
                    isError = false,
                    layout = layout,
                )
            }
        }
    }

    fun loadSearchHistory() {
        val history = sharedPreferences.getString("searchHistory", null)

        if (history != null) {
            _state.update {
                it.copy(searchHistory = json.decodeFromString(history))
            }
        } else {
            _state.update {
                it.copy(searchHistory = listOf())
            }
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
        sharedPreferences.edit().putString("searchHistory", json.encodeToString(history)).apply()
        _state.update { it.copy(searchHistory = history) }
    }

    fun search(query: String) {
        if (query.isEmpty()) {
            return
        }

        _state.update { it.copy(isSearchLoading = true) }

        viewModelScope.launch {
            try {
                searchApi.searchGet(query, type = null, page = "1", limit = "10").let { response ->
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
            } catch (_: UnknownHostException) {
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

    fun clearSearchHistory() {
        sharedPreferences.edit().remove("searchHistory").apply()
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
        viewModelScope.launch {
            val response = playlistsApi.playlistsGet()

            if (!response.success) {
                return@launch
            }

            _state.update {
                it.copy(playlists = response.body().playlists)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val response = playlistsApi.playlistsPost(CreatePlaylistRequest(name))

            _state.update {
                it.copy(playlists = it.playlists.plus(PlaylistSummary(
                    id = response.body().id,
                    name = name,
                    songsCount = 0
                )))
            }
        }
    }
}