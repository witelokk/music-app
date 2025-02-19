package com.witelokk.musicapp.data

import kotlin.time.Duration

data class PlayerState(
    val song: Song,
    val playing: Boolean,
    val currentPosition: Duration,
    val previousTrackAvailable: Boolean,
    val nextTrackAvailable: Boolean,
)
