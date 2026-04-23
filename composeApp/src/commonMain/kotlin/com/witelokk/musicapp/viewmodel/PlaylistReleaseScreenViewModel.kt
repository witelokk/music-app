package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.ReleasesApi
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.ConnectionErrorException
import com.witelokk.musicapp.repository.PlaylistsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistReleaseScreenState(
    val isLoading: Boolean = true,
    val hasObservedFeed: Boolean = false,
    val hasCachedFeed: Boolean = false,
    val isConnectionError: Boolean = false,
    val isError: Boolean = false,
    val deleted: Boolean = false,
    val songs: List<Song> = listOf(),
    val name: String = "",
    val coverUrl: String? = null,
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState?,
)

class PlaylistReleaseScreenViewModel(
    private val playlistsApi: PlaylistsApi,
    private val releasesApi: ReleasesApi,
    private val playlistsRepository: PlaylistsRepository,
    favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer,
) : BaseViewModel(musicPlayer, favoritesApi, playlistsApi) {
    private val _state =
        MutableStateFlow(PlaylistReleaseScreenState(playerState = musicPlayer.state.value))
    val state = _state.asStateFlow()

    private var playlistId: String? = null
    private var currentPlaylistId: String? = null
    private var observePlaylistJob: Job? = null

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
        playlistId = id
        observePlaylist(id)
        launchCatching(action = "load playlist $id", onError = {
            _state.update { state ->
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

    fun loadRelease(id: String) {
        playlistId = null
        currentPlaylistId = null
        observePlaylistJob?.cancel()
        _state.update { PlaylistReleaseScreenState(playerState = it.playerState) }
        launchCatching(action = "load release $id", onError = {
            _state.update { state ->
                state.copy(
                    isConnectionError = it is ConnectionErrorException,
                    isError = true,
                    isLoading = false,
                )
            }
        }) {
            val response = releasesApi.getRelease(id)

            if (response.logIfFailure("load release $id")) {
                _state.update { it.copy(isError = true, isLoading = false) }
                return@launchCatching
            }

            val release = response.body()

            _state.update {
                it.copy(
                    isLoading = false,
                    hasObservedFeed = true,
                    hasCachedFeed = false,
                    isConnectionError = false,
                    isError = false,
                    songs = release.songs.songs,
                    name = release.name,
                    coverUrl = release.coverUrl,
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

    fun toggleSongFavorite(song: Song) {
        changeSongFavorite(song, !song.isFavorite)
    }

    fun loadPlaylists() {
        launchCatching(action = "load playlists for playlist/release screen") {
            _state.update {
                it.copy(playlists = playlistsRepository.getPlaylists())
            }
        }
    }

    fun deletePlaylist() {
        playlistId?.let { currentPlaylistId ->
            launchCatching(action = "delete playlist $currentPlaylistId") {
                playlistsRepository.deletePlaylist(currentPlaylistId)
                _state.update { it.copy(deleted = true) }
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
        _state.update { currentState ->
            currentState.copy(
                songs = currentState.songs.map {
                    if (song.id == it.id) song.copy(isFavorite = favorite)
                    else it
                }
            )
        }

        val playlistId = currentPlaylistId ?: return
        launchCatching(action = "update cached favorite for song ${song.id} on playlist/release screen") {
            playlistsRepository.updateCachedSongFavorite(playlistId, song.id, favorite)
        }
    }

    override fun addSongToPlaylists(song: Song, playlistIds: List<String>) {
        super.addSongToPlaylists(song, playlistIds)

        if (playlistId in playlistIds) {
            _state.update {
                it.copy(songs = listOf(song) + it.songs.filterNot { current -> current.id == song.id })
            }
            playlistId?.let { currentPlaylistId ->
                launchCatching(action = "cache song ${song.id} for playlist $currentPlaylistId") {
                    playlistsRepository.prependSongToPlaylist(currentPlaylistId, song)
                }
            }
        }
    }

    private fun observePlaylist(id: String) {
        if (currentPlaylistId == id) {
            return
        }

        currentPlaylistId = id
        _state.update { PlaylistReleaseScreenState(playerState = it.playerState) }

        observePlaylistJob?.cancel()
        observePlaylistJob = viewModelScope.launch {
            var isInitialEmission = true

            playlistsRepository.observePlaylist(id).collect { playlist ->
                _state.update {
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
