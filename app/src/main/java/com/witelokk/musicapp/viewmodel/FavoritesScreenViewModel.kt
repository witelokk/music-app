package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.BaseViewModel
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.AddFavoriteSongRequest
import com.witelokk.musicapp.api.models.Playlist
import com.witelokk.musicapp.api.models.RemoveFavoriteSongRequest
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesScreenState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val songs: List<Song> = listOf(),
    val playerState: PlayerState?,
)

class FavoritesScreenViewModel(
    private val favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer
) : BaseViewModel(musicPlayer) {
    private val _state =
        MutableStateFlow(FavoritesScreenState(playerState = musicPlayer.state.value))
    val state = _state.asStateFlow()

    init {
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
        viewModelScope.launch {
            val response = favoritesApi.favoritesGet()

            if (!response.success) {
                _state.update { it.copy(isError = true, isLoading = false) }
                return@launch
            }

            val favorites = response.body()

            _state.update {
                it.copy(
                    isLoading = false,
                    isError = false,
                    songs = favorites.songs,
                )
            }
        }
    }

    fun playSong(song: Song) {
        musicPlayer.setQueueAndPlay(state.value.songs, state.value.songs.indexOf(song))
    }

    fun playAllSongs() {
        musicPlayer.setQueueAndPlay(state.value.songs, 0)
    }

    fun removeSongFromFavorites(song: Song) {
        viewModelScope.launch {
            favoritesApi.favoritesDelete(RemoveFavoriteSongRequest(song.id))

            _state.update { currentState ->
                currentState.copy(
                    songs = currentState.songs.filter { it != song }
                )
            }
        }
    }
}