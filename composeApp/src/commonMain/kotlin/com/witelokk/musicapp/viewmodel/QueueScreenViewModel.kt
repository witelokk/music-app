package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.FavoriteSongRequest
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QueueScreenState(
    val songs: List<Song> = listOf(),
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState?,
)

class QueueScreenViewModel(
    private val favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer,
    private val playlistsApi: PlaylistsApi,
    private val mediaCache: MediaCache,
) : BaseViewModel(musicPlayer, favoritesApi, playlistsApi, mediaCache) {
    private val _state =
        MutableStateFlow(
            QueueScreenState(
                playerState = musicPlayer.state.value,
                songs = musicPlayer.state.value?.queue ?: listOf()
            )
        )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            musicPlayer.state.collect { playerState ->
                val currentIndex = playerState?.queue?.indexOf(playerState.currentSong) ?: -1
                if (currentIndex > 0) {
                    _state.update {
                        it.copy(
                            playerState = playerState,
                            songs = playerState?.queue?.subList(
                                currentIndex,
                                playerState.queue.size
                            ) ?: listOf()
                        )
                    }
                }
            }
        }
    }

    fun toggleSongFavorite(song: Song) {
        launchCatching(action = "toggle favorite for song ${song.id} in queue") {
            if (song.isFavorite) {
                favoritesApi.removeFavorite(song.id)
            } else {
                favoritesApi.addFavorite(FavoriteSongRequest(song.id))
            }

            musicPlayer.updateSong(song.copy(isFavorite = !song.isFavorite))

            _state.update { currentState ->
                currentState.copy(
                    songs = currentState.songs.map {
                        if (song.id == it.id) song.copy(isFavorite = !song.isFavorite)
                        else it
                    }
                )
            }
        }
    }

    fun playSong(song: Song) {
        musicPlayer.state.value?.queue?.let { musicPlayer.playSongInQueue(it.indexOf(song)) }
    }

    fun loadPlaylists() {
        launchCatching(action = "load playlists for queue screen") {
            val response = playlistsApi.getPlaylists()

            if (response.logIfFailure("load playlists for queue screen")) {
                return@launchCatching
            }

            _state.update {
                it.copy(playlists = response.body().playlists)
            }
        }
    }

    fun removeSongFromQueue(index: Int) {
        musicPlayer.state.value?.let { playerState ->
            musicPlayer.removeFromQueue(index + playerState.queue.indexOf(playerState.currentSong))
        }
    }

    override fun changeSongFavorite(song: Song, favorite: Boolean) {
        super.changeSongFavorite(song, favorite)
        _state.update { currentState ->
            currentState.copy(
                songs = currentState.songs.map {
                    if (song.id == it.id) song.copy(isFavorite = favorite)
                    else it
                }
            )
        }
    }
}
