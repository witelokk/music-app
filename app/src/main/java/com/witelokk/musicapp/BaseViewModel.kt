package com.witelokk.musicapp

import androidx.lifecycle.ViewModel
import com.witelokk.musicapp.api.models.Song
import kotlin.time.Duration

open class BaseViewModel(private val musicPlayer: MusicPlayer): ViewModel() {
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

    fun setPlayerQueueAndPlay(songs: List<Song>, index: Int) {
        musicPlayer.setQueueAndPlay(songs, index)
    }
}