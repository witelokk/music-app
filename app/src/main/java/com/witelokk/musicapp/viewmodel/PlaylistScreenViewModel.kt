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
import kotlinx.serialization.Serializable

data class PlaylistScreenState(
    val isLoading: Boolean = true,
    var isError: Boolean = false,
    val playlist: Playlist? = null,
    val playerState: PlayerState?,
)

class PlaylistScreenViewModel(
    private val playlistsApi: PlaylistsApi,
    private val favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer
) : BaseViewModel(musicPlayer) {
    private val _state = MutableStateFlow(PlaylistScreenState(playerState = musicPlayer.state.value))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            musicPlayer.state.collect { newPlayerState ->
                _state.update { currentState ->
                    currentState.copy(playerState = newPlayerState)
                }
            }
        }
    }

    fun loadPlaylist(id: String) {
        viewModelScope.launch {
            val response = playlistsApi.playlistsIdGet(id)

            if (!response.success) {
                _state.update { it.copy(isError = true, isLoading = false) }
                return@launch
            }

            val playlist = response.body()

            _state.update {
                it.copy(
                    isLoading = false,
                    isError = false,
                    playlist = playlist,
                )
            }
        }
    }

    fun playSong(song: Song) {
        state.value.playlist?.let { playlist ->
            musicPlayer.setQueueAndPlay(playlist.songs.songs, playlist.songs.songs.indexOf(song))
        }
    }

    fun playAllSongs() {
        state.value.playlist?.let { playlist ->
            musicPlayer.setQueueAndPlay(playlist.songs.songs, 0)
        }
    }


    fun removeSongFromFavorites(song: Song) {
        viewModelScope.launch {
            if (song.isFavorite) {
                favoritesApi.favoritesDelete(RemoveFavoriteSongRequest(song.id))
            } else {
                favoritesApi.favoritesPost(AddFavoriteSongRequest(song.id))
            }

            _state.update { currentState ->
                currentState.copy(
                    playlist = currentState.playlist?.copy(
                        songs = currentState.playlist.songs.copy(
                            songs = currentState.playlist.songs.songs.map {
                                if (song.id == it.id) {
                                    song.copy(isFavorite = !song.isFavorite)
                                } else song
                            }
                        )
                    )
                )
            }
        }
    }
}