package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.AddFavoriteSongRequest
import com.witelokk.musicapp.api.models.AddSongToPlaylistRequest
import com.witelokk.musicapp.api.models.RemoveFavoriteSongRequest
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

abstract class BaseViewModel(
    private val musicPlayer: MusicPlayer,
    private val favoritesApi: FavoritesApi,
    private val playlistsApi: PlaylistsApi
) : ViewModel() {

    protected val playerState: StateFlow<PlayerState?> = musicPlayer.state

    fun seekPlayer(to: Duration) {
        musicPlayer.seek(to)
    }

    fun seekPlayerToNext() {
        musicPlayer.seekToNext()
    }

    fun seekPlayerToPrevious() {
        musicPlayer.seekToPrevious()
    }

    fun playPausePlayer() {
        musicPlayer.playPause()
    }

    fun addSongToQueue(song: Song) {
        musicPlayer.addToQueue(song)
    }

    fun setPlayerQueueAndPlay(songs: List<Song>, index: Int) {
        musicPlayer.setQueueAndPlay(songs, index)
    }

    open fun addSongToPlaylists(song: Song, playlistIds: List<String>) {
        launchCatching(action = "add song ${song.id} to playlists") {
            for (playlistId in playlistIds) {
                playlistsApi.playlistsIdSongsPost(playlistId, AddSongToPlaylistRequest(song.id))
            }
        }
    }

    open fun changeSongFavorite(song: Song, favorite: Boolean) {
        launchCatching(action = "change favorite for song ${song.id}") {
            if (song.isFavorite) {
                favoritesApi.favoritesDelete(RemoveFavoriteSongRequest(song.id))
            } else {
                favoritesApi.favoritesPost(AddFavoriteSongRequest(song.id))
            }
        }
        musicPlayer.updateSong(song.copy(isFavorite = favorite))
    }

    fun playSongInQueue(index: Int) {
        musicPlayer.playSongInQueue(index)
    }
}
