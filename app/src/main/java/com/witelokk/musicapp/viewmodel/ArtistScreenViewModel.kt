package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.api.apis.ArtistsApi
import com.witelokk.musicapp.api.models.Artist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class ArtistScreenState(
    val artist: Artist? = null,
    val isLoading: Boolean = true,
    var isError: Boolean = false
)

class ArtistScreenViewModel(
    private val api: ArtistsApi,
): ViewModel() {
    private val _state = MutableStateFlow(ArtistScreenState())
    val state = _state.asStateFlow()

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            val response = api.artistsIdGet(artistId)

            if (!response.success) {
                _state.update { it.copy(isError = true, isLoading = false) }
                return@launch
            }

            val artist = response.body()

            _state.update { it.copy(
                isLoading = false,
                isError =  false,
                artist = artist,
//                artist = Artist(
//                    name = artist.name,
//                    followers = artist.followers,
//                    following = artist.following,
//                    avatarUrl = artist.avatarUrl,
//                    coverUrl = artist.coverUrl,
////                    popularSongs = artist.popularSongs.songs.map {
////                        Song(
////                            coverUrl = it.coverUrl,
////                            name = it.name,
////                            artists = it.artists.map {
////                                Artist(
////                                    name = it.name,
////                                    followers = 0,
////                                    following = false,
////                                    avatarUrl = it.avatarUrl,
////                                    coverUrl = it.avatarUrl,
////                                    popularSongs = emptyList()
////                                )
////                            },
////                            duration = it.durationSeconds.seconds,
////                            liked = it.isFavorite,
////                        )
////                    },
//                    popularSongs = artist.popularSongs.songs,
//                    releases = artist.releases.releases,
//                )
            ) }
        }
    }
}