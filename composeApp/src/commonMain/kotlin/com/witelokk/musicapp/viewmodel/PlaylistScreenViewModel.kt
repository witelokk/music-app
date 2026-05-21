package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.ConnectionErrorException
import com.witelokk.musicapp.repository.PlaylistsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistScreenState(
    override val isLoading: Boolean = true,
    val hasObservedFeed: Boolean = false,
    val hasCachedFeed: Boolean = false,
    override val isConnectionError: Boolean = false,
    override val isError: Boolean = false,
    val deleted: Boolean = false,
    override val songs: List<Song> = listOf(),
    val name: String = "",
    val coverUrl: String? = null,
    override val playlists: List<PlaylistSummary> = listOf(),
    override val playerState: PlayerState?,
) : SongListScreenState

class PlaylistScreenViewModel(
    private val playlistsApi: PlaylistsApi,
    private val playlistsRepository: PlaylistsRepository,
    favoritesApi: FavoritesApi,
    musicPlayer: MusicPlayer,
    mediaCache: MediaCache,
) : SongListViewModel<PlaylistScreenState>(
    musicPlayer,
    favoritesApi,
    playlistsApi,
    mediaCache,
    playlistsRepository
) {
    private val _state = MutableStateFlow(PlaylistScreenState(playerState = musicPlayer.state.value))
    override val state: StateFlow<PlaylistScreenState> = _state.asStateFlow()

    private var playlistId: String? = null
    private var observePlaylistJob: Job? = null

    override fun MutableStateFlowAccessor(): MutableStateFlow<PlaylistScreenState> = _state

    override fun copyWithPlayerState(
        state: PlaylistScreenState,
        playerState: PlayerState?
    ) = state.copy(playerState = playerState)

    override fun copyWithPlaylists(
        state: PlaylistScreenState,
        playlists: List<PlaylistSummary>
    ) = state.copy(playlists = playlists)

    override fun copyWithSongs(
        state: PlaylistScreenState,
        songs: List<Song>
    ) = state.copy(songs = songs)

    init {
        bindPlayerState()
    }

    fun loadPlaylist(id: String) {
        playlistId = id
        observePlaylist(id)
        launchCatching(action = "load playlist $id", onError = {
            updateState { state ->
                state.copy(
                    isConnectionError = it is ConnectionErrorException,
                    isError = true,
                    isLoading = false,
                )
            }
        }) {
            playlistsRepository.refreshPlaylist(id)
        }
    }

    fun deletePlaylist() {
        playlistId?.let { currentPlaylistId ->
            launchCatching(action = "delete playlist $currentPlaylistId") {
                playlistsRepository.deletePlaylist(currentPlaylistId)
                updateState { it.copy(deleted = true) }
            }
        }
    }

    fun editPlaylistName(name: String) {
        playlistId?.let { currentPlaylistId ->
            launchCatching(action = "rename playlist $currentPlaylistId") {
                playlistsRepository.renamePlaylist(currentPlaylistId, name)
            }
        }
    }

    fun removeSongFromPlaylist(song: Song) {
        playlistId?.let { currentPlaylistId ->
            launchCatching(action = "remove song ${song.id} from playlist $currentPlaylistId") {
                playlistsRepository.removeSongFromPlaylist(currentPlaylistId, song.id)
            }
        }
    }

    override fun changeSongFavorite(song: Song, favorite: Boolean) {
        super.changeSongFavorite(song, favorite)

        val currentPlaylistId = playlistId ?: return
        launchCatching(action = "update cached favorite for song ${song.id} on playlist screen") {
            playlistsRepository.updateCachedSongFavorite(currentPlaylistId, song.id, favorite)
        }
    }

    override fun addSongToPlaylists(song: Song, playlistIds: List<String>) {
        super.addSongToPlaylists(song, playlistIds)

        val currentPlaylistId = playlistId ?: return
        if (currentPlaylistId in playlistIds) {
            updateSongs { songs ->
                listOf(song) + songs.filterNot { currentSong -> currentSong.id == song.id }
            }
            launchCatching(action = "cache song ${song.id} for playlist $currentPlaylistId") {
                playlistsRepository.prependSongToPlaylist(currentPlaylistId, song)
            }
        }
    }

    private fun observePlaylist(id: String) {
        if (playlistId == id && state.value.hasObservedFeed) {
            return
        }

        updateState { PlaylistScreenState(playerState = it.playerState) }

        observePlaylistJob?.cancel()
        observePlaylistJob = viewModelScope.launch {
            var isInitialEmission = true

            playlistsRepository.observePlaylist(id).collect { playlist ->
                updateState {
                    it.copy(
                        isLoading = isInitialEmission && playlist == null,
                        hasObservedFeed = true,
                        hasCachedFeed = playlist != null || it.hasCachedFeed,
                        isConnectionError = false,
                        isError = false,
                        songs = playlist?.songs ?: it.songs,
                        name = playlist?.name ?: it.name,
                        coverUrl = playlist?.coverUrl ?: it.coverUrl,
                    )
                }

                isInitialEmission = false
            }
        }
    }
}
