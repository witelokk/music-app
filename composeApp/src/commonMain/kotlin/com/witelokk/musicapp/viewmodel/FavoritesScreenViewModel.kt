package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.cache.MediaCacheState
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.ConnectionErrorException
import com.witelokk.musicapp.repository.FavoritesRepository
import com.witelokk.musicapp.repository.PlaylistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesScreenState(
    override val isLoading: Boolean = false,
    val hasObservedFavorites: Boolean = false,
    override val isConnectionError: Boolean = false,
    override val isError: Boolean = false,
    val snackbarEventId: Long = 0,
    override val songs: List<Song> = listOf(),
    override val playlists: List<PlaylistSummary> = listOf(),
    override val playerState: PlayerState?,
) : SongListScreenState

class FavoritesScreenViewModel(
    favoritesApi: FavoritesApi,
    private val favoritesRepository: FavoritesRepository,
    private val musicPlayer: MusicPlayer,
    private val playlistsApi: PlaylistsApi,
    private val mediaCache: MediaCache,
    playlistsRepository: PlaylistsRepository,
) : SongListViewModel<FavoritesScreenState>(
    musicPlayer,
    favoritesApi,
    playlistsApi,
    mediaCache,
    playlistsRepository
) {
    private val _state =
        MutableStateFlow(FavoritesScreenState(playerState = musicPlayer.state.value))
    override val state: StateFlow<FavoritesScreenState> = _state.asStateFlow()

    override fun MutableStateFlowAccessor(): MutableStateFlow<FavoritesScreenState> = _state

    override fun copyWithPlayerState(
        state: FavoritesScreenState,
        playerState: PlayerState?
    ) = state.copy(playerState = playerState)

    override fun copyWithPlaylists(
        state: FavoritesScreenState,
        playlists: List<PlaylistSummary>
    ) = state.copy(playlists = playlists)

    override fun copyWithSongs(
        state: FavoritesScreenState,
        songs: List<Song>
    ) = state.copy(songs = songs)

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

                // cache favorite songs
                favorites?.forEach { song ->
                    if (mediaCache.getCacheState(song.streamUrl).first() == MediaCacheState.NOT_CACHED) {
                        mediaCache.cache(song.streamUrl)
                    }
                }

                isInitialEmission = false
            }
        }

        loadFavorites()
        bindPlayerState()
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

            updatePlayerSong(song.copy(isFavorite = favorite))
        }
    }
}
