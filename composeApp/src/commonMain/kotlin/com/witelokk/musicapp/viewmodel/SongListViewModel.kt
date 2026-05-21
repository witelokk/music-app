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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class SongListViewModel<S : SongListScreenState>(
    musicPlayer: MusicPlayer,
    favoritesApi: FavoritesApi,
    playlistsApi: PlaylistsApi,
    mediaCache: MediaCache,
    private val playlistsRepository: PlaylistsRepository,
) : BaseViewModel(musicPlayer, favoritesApi, playlistsApi, mediaCache) {

    abstract val state: StateFlow<S>

    protected abstract fun MutableStateFlowAccessor(): kotlinx.coroutines.flow.MutableStateFlow<S>
    protected abstract fun copyWithPlayerState(state: S, playerState: PlayerState?): S
    protected abstract fun copyWithPlaylists(state: S, playlists: List<PlaylistSummary>): S
    protected abstract fun copyWithSongs(state: S, songs: List<Song>): S

    protected fun bindPlayerState() {
        viewModelScope.launch {
            playerState.collect { newPlayerState ->
                MutableStateFlowAccessor().update { currentState ->
                    copyWithPlayerState(currentState, newPlayerState)
                }
            }
        }
    }

    protected fun updateState(transform: (S) -> S) {
        MutableStateFlowAccessor().update(transform)
    }

    protected fun currentState(): S = state.value

    protected fun updateSongs(transform: (List<Song>) -> List<Song>) {
        updateState { currentState ->
            copyWithSongs(currentState, transform(currentState.songs))
        }
    }

    protected fun updateSongFavoriteInState(song: Song, favorite: Boolean) {
        updateSongs { songs ->
            songs.map { currentSong ->
                if (currentSong.id == song.id) currentSong.copy(isFavorite = favorite)
                else currentSong
            }
        }
    }

    open fun playSong(song: Song) {
        playSongFromCollection(song, state.value.songs)
    }

    open fun playAllSongs() {
        playCollection(state.value.songs)
    }

    open fun toggleSongFavorite(song: Song) {
        changeSongFavorite(song, !song.isFavorite)
    }

    open fun loadPlaylists() {
        launchCatching(action = "load playlists for song list screen") {
            val playlists = playlistsRepository.getPlaylists()
            updateState { currentState ->
                copyWithPlaylists(currentState, playlists)
            }
        }
    }

    override fun changeSongFavorite(song: Song, favorite: Boolean) {
        super.changeSongFavorite(song, favorite)
        updateSongFavoriteInState(song, favorite)
    }
}
