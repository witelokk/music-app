package com.witelokk.musicapp.data

import com.witelokk.musicapp.api.models.Song
import kotlin.time.Duration

data class PlayerState(
    val currentSong: Song,
    val currentSongIndex: Int,
    val playing: Boolean,
    val currentPosition: Duration,
    val previousTrackAvailable: Boolean,
    val nextTrackAvailable: Boolean,
    val queue: List<Song>,
)
