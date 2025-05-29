package com.witelokk.musicapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.AddSongToPlaylistRequest
import com.witelokk.musicapp.api.models.Song
import kotlinx.coroutines.launch
import kotlin.time.Duration

abstract class BaseViewModel(
    private val musicPlayer: MusicPlayer,
    private val playlistsApi: PlaylistsApi
): ViewModel() {

    protected val playerState = musicPlayer.state

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
        viewModelScope.launch {
            for (playlistId in playlistIds) {
                playlistsApi.playlistsIdSongsPost(playlistId, AddSongToPlaylistRequest(song.id))
            }
        }
    }

    open fun changeSongFavorite(song: Song, favorite: Boolean) {
        musicPlayer.updateSong(song.copy(isFavorite = favorite))
    }

    fun playSongInQueue(index: Int) {
        musicPlayer.playSongInQueue(index)
    }
}