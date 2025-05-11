package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.BaseViewModel
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.ArtistsApi
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.AddFavoriteSongRequest
import com.witelokk.musicapp.api.models.Artist
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.RemoveFavoriteSongRequest
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArtistScreenState(
    val artist: Artist? = null,
    val isLoading: Boolean = true,
    var isError: Boolean = false,
    val selectedReleaseType: String? = null,
    val playlists: List<PlaylistSummary> = listOf(),
    val playerState: PlayerState? = null,
) {
    val filteredArtist: Artist?
        get() = if (selectedReleaseType == null) {
            artist
        } else if (artist != null) {
            val filteredReleases =
                artist.releases.releases.filter { it.type == selectedReleaseType }
            artist.copy(releases = artist.releases.copy(releases = filteredReleases))
        } else {
            null
        }
}

class ArtistScreenViewModel(
    private val artistsApi: ArtistsApi,
    private val favoritesApi: FavoritesApi,
    private val playlistsApi: PlaylistsApi,
    private val musicPlayer: MusicPlayer,
) : BaseViewModel(musicPlayer, playlistsApi) {
    private val _state = MutableStateFlow(ArtistScreenState(playerState = musicPlayer.state.value))
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

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            val response = artistsApi.artistsIdGet(artistId)

            if (!response.success) {
                _state.update { it.copy(isError = true, isLoading = false) }
                return@launch
            }

            val artist = response.body()

            _state.update {
                it.copy(
                    isLoading = false,
                    isError = false,
                    artist = artist,
                )
            }
        }
    }

    fun playAllSongs() {
        _state.value.artist?.let { artist ->
            musicPlayer.setQueueAndPlay(artist.popularSongs.songs, 0)
        }
    }

    fun playPopularSong(song: Song) {
        _state.value.artist?.let { artist ->
            musicPlayer.setQueueAndPlay(
                artist.popularSongs.songs,
                artist.popularSongs.songs.indexOf(song)
            )
        }
    }

    fun toggleSongFavorite(song: Song) {
        _state.value.artist?.let { artist ->
            viewModelScope.launch {
                if (song.isFavorite) {
                    favoritesApi.favoritesDelete(RemoveFavoriteSongRequest(song.id))
                } else {
                    favoritesApi.favoritesPost(AddFavoriteSongRequest(song.id))
                }

                _state.update {
                    val updatedSongs = artist.popularSongs.songs.map { updatedSong ->
                        if (updatedSong.id == song.id) updatedSong.copy(isFavorite = !song.isFavorite) else updatedSong
                    }
                    val updatedPopularSongs = artist.popularSongs.copy(songs = updatedSongs)
                    val updatedArtist = artist.copy(popularSongs = updatedPopularSongs)

                    it.copy(artist = updatedArtist)
                }
            }
        }
    }

    fun filterReleases(type: String?) {
        _state.update {
            it.copy(selectedReleaseType = type)
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
}