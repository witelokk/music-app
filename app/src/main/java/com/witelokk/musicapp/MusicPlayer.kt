package com.witelokk.musicapp

import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MusicPlayer {
    private val _state = MutableStateFlow<PlayerState?>(null)
    val state = _state.asStateFlow()

    fun playPause() {
        _state.update { it?.copy(playing = !it.playing) }
    }

    fun seek(to: Duration) {
        _state.update { it?.copy(currentPosition = to) }
    }
}