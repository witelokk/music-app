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

data class QueueScreenState(
    val songs: List<Song> = listOf(),
    val playerState: PlayerState?,
)

class QueueScreenViewModel(
    private val favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer,
    private val playlistsApi: PlaylistsApi,
) : BaseViewModel(musicPlayer, playlistsApi) {
    private val _state =
        MutableStateFlow(
            FavoritesScreenState(
                playerState = musicPlayer.state.value,
                songs = listOf(), //musicPlayer.state.value?.queue ?: listOf()
            )
        )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            musicPlayer.state.collect { playerState ->
                _state.update {
                    it.copy(
                        playerState = playerState,
                        songs = playerState?.queue?.subList(
                            playerState.queue.indexOf(playerState.song),
                            playerState.queue.size
                        ) ?: listOf()
                    )
                }
            }
        }
    }

    fun removeSongFromFavorites(song: Song) {
//        viewModelScope.launch {
//            favoritesApi.favoritesDelete(RemoveFavoriteSongRequest(song.id))
//
//            _state.update { currentState ->
//                currentState.copy(
//                    songs = currentState.songs.filter { it != song }
//                )
//            }
//        }
    }

    fun playSong(song: Song) {
        musicPlayer.state.value?.queue?.let { musicPlayer.playSongInQueue(it.indexOf(song)) }
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

    fun removeSongFromQueue(index: Int) {
        musicPlayer.state.value?.let { playerState ->
            musicPlayer.removeFromQueue(index+playerState.queue.indexOf(playerState.song))
        }
    }
}