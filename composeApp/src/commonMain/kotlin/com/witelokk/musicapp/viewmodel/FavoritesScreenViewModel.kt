package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.ConnectionErrorException
import com.witelokk.musicapp.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesScreenState(
    val isLoading: Boolean = false,
    val hasObservedFavorites: Boolean = false,
    val isConnectionError: Boolean = false,
    val isError: Boolean = false,
    val snackbarEventId: Long = 0,
    val songs: List<Song> = listOf(),
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState?,
)

class FavoritesScreenViewModel(
    favoritesApi: FavoritesApi,
    private val favoritesRepository: FavoritesRepository,
    private val musicPlayer: MusicPlayer,
    private val playlistsApi: PlaylistsApi,
) : BaseViewModel(musicPlayer, favoritesApi, playlistsApi) {
    private val _state =
        MutableStateFlow(FavoritesScreenState(playerState = musicPlayer.state.value))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            var isInitialEmission = true

            favoritesRepository.observeFavorites().collect { favorites ->
                _state.update {
                    it.copy(
                        isLoading = isInitialEmission && favorites == null,
                        hasObservedFavorites = true,
                        isConnectionError = false,
                        isError = false,
                        songs = favorites ?: it.songs,
                    )
                }

                isInitialEmission = false
            }
        }

        loadFavorites()

        viewModelScope.launch {
            musicPlayer.state.collect { newPlayerState ->
                _state.update { currentState ->
                    currentState.copy(playerState = newPlayerState)
                }
            }
        }
    }

    fun loadFavorites() {
        launchCatching(action = "load favorites", onError = {
            _state.update { state ->
                state.copy(
                    isLoading = false,
                    hasObservedFavorites = true,
                    isConnectionError = it is ConnectionErrorException,
                    isError = it !is ConnectionErrorException,
                )
            }
        }) {
            favoritesRepository.refreshFavorites()
        }
    }

    fun playSong(song: Song) {
        musicPlayer.setQueueAndPlay(state.value.songs, state.value.songs.indexOf(song))
    }

    fun playAllSongs() {
        musicPlayer.setQueueAndPlay(state.value.songs, 0)
    }

    fun loadPlaylists() {
        launchCatching(action = "load playlists for favorites screen") {
            val response = playlistsApi.getPlaylists()

            if (response.logIfFailure("load playlists for favorites screen")) {
                return@launchCatching
            }

            _state.update {
                it.copy(playlists = response.body().playlists)
            }
        }
    }

    override fun changeSongFavorite(song: Song, favorite: Boolean) {
        launchCatching(
            action = "change favorite for song ${song.id} on favorites screen",
            onError = {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        hasObservedFavorites = true,
                        isConnectionError = it is ConnectionErrorException,
                        isError = it !is ConnectionErrorException,
                        snackbarEventId = state.snackbarEventId + 1,
                    )
                }
            }) {
            if (favorite) {
                favoritesRepository.addFavorite(song)
            } else {
                favoritesRepository.removeFavorite(song.id)
            }

            musicPlayer.updateSong(song.copy(isFavorite = favorite))
        }
    }
}
