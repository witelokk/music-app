package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.BaseViewModel
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.AddFavoriteSongRequest
import com.witelokk.musicapp.api.models.PlaylistSummary
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
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState?,
)

class FavoritesScreenViewModel(
    private val favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer,
    private val playlistsApi: PlaylistsApi,
) : BaseViewModel(musicPlayer, favoritesApi, playlistsApi) {
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

//    fun removeSongFromFavorites(song: Song) {
//        viewModelScope.launch {
//            favoritesApi.favoritesDelete(RemoveFavoriteSongRequest(song.id))
//
//            _state.update { currentState ->
//                currentState.copy(
//                    songs = currentState.songs.filter { it != song }
//                )
//            }
//
//            musicPlayer.updateSong(song.copy(isFavorite = false))
//        }
//    }

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

    override fun changeSongFavorite(song: Song, favorite: Boolean) {
        super.changeSongFavorite(song, favorite)
        _state.update { currentState ->
            currentState.copy(
                songs = if (favorite) listOf(song.copy(isFavorite = true)).plus(currentState.songs)
                else currentState.songs.filter { it != song }
            )
        }
    }
}