package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.BaseViewModel
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.ReleasesApi
import com.witelokk.musicapp.api.models.AddFavoriteSongRequest
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.RemoveFavoriteSongRequest
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.api.models.UpdatePlaylistRequest
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistReleaseScreenState(
    val isLoading: Boolean = true,
    var isError: Boolean = false,
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
    private val favoritesApi: FavoritesApi,
    private val musicPlayer: MusicPlayer
) : BaseViewModel(musicPlayer, playlistsApi) {
    private val _state =
        MutableStateFlow(PlaylistReleaseScreenState(playerState = musicPlayer.state.value))
    val state = _state.asStateFlow()
    private var playlistId: String? = null

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
                    songs = playlist.songs.songs,
                    name = playlist.name,
                    coverUrl = playlist.coverUrl,
                )
            }
        }
    }

    fun loadRelease(id: String) {
        viewModelScope.launch {
            val response = releasesApi.releasesIdGet(id)

            if (!response.success) {
                _state.update { it.copy(isError = true, isLoading = false) }
                return@launch
            }

            val release = response.body()

            _state.update {
                it.copy(
                    isLoading = false,
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
        viewModelScope.launch {
            if (song.isFavorite) {
                favoritesApi.favoritesDelete(RemoveFavoriteSongRequest(song.id))
            } else {
                favoritesApi.favoritesPost(AddFavoriteSongRequest(song.id))
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

    fun deletePlaylist() {
        playlistId?.let { playlistId ->
            viewModelScope.launch {
                playlistsApi.playlistsIdDelete(playlistId)

                _state.update { it.copy(deleted = true) }
            }
        }
        _state.update { it.copy(deleted = true) }
    }

    fun editPlaylistName(name: String) {
        playlistId?.let { playlistId ->
            viewModelScope.launch {
                playlistsApi.playlistsIdPut(playlistId, UpdatePlaylistRequest(name))

                _state.update { it.copy(name = name) }
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
    }

    override fun addSongToPlaylists(song: Song, playlistIds: List<String>) {
        super.addSongToPlaylists(song, playlistIds)

        if (playlistId in playlistIds) {
            _state.update { it.copy(songs = listOf(song) + it.songs) }
        }
    }
}