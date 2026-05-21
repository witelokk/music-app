package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.PlaylistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QueueScreenState(
    override val isLoading: Boolean = false,
    override val isConnectionError: Boolean = false,
    override val isError: Boolean = false,
    override val songs: List<Song> = listOf(),
    override val playlists: List<PlaylistSummary> = listOf(),
    override val playerState: PlayerState?,
) : SongListScreenState

class QueueScreenViewModel(
    favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer,
    playlistsApi: PlaylistsApi,
    private val mediaCache: MediaCache,
    playlistsRepository: PlaylistsRepository,
) : SongListViewModel<QueueScreenState>(
    musicPlayer,
    favoritesApi,
    playlistsApi,
    mediaCache,
    playlistsRepository
) {
    private val _state =
        MutableStateFlow(
            QueueScreenState(
                playerState = musicPlayer.state.value,
                songs = musicPlayer.state.value?.queue ?: listOf()
            )
        )
    override val state: StateFlow<QueueScreenState> = _state.asStateFlow()

    override fun MutableStateFlowAccessor(): MutableStateFlow<QueueScreenState> = _state

    override fun copyWithPlayerState(
        state: QueueScreenState,
        playerState: PlayerState?
    ) = state.copy(playerState = playerState)

    override fun copyWithPlaylists(
        state: QueueScreenState,
        playlists: List<PlaylistSummary>
    ) = state.copy(playlists = playlists)

    override fun copyWithSongs(
        state: QueueScreenState,
        songs: List<Song>
    ) = state.copy(songs = songs)

    init {
        viewModelScope.launch {
            musicPlayer.state.collect { playerState ->
                val currentIndex = playerState?.queue?.indexOf(playerState.currentSong) ?: -1
                _state.update {
                    it.copy(
                        playerState = playerState,
                        songs = if (currentIndex >= 0 && playerState != null) {
                            playerState.queue.subList(currentIndex, playerState.queue.size)
                        } else {
                            listOf()
                        }
                    )
                }
            }
        }
    }

    override fun playSong(song: Song, offline: Boolean) {
        musicPlayer.state.value?.queue?.let { musicPlayer.playSongInQueue(it.indexOf(song)) }
    }

    fun removeSongFromQueue(index: Int) {
        musicPlayer.state.value?.let { playerState ->
            musicPlayer.removeFromQueue(index + playerState.queue.indexOf(playerState.currentSong))
        }
    }
}
