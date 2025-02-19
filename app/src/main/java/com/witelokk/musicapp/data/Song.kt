package com.witelokk.musicapp.data

import kotlin.time.Duration

data class Song(
    val cover: String,
    val name: String,
    val artists: List<Artist>,
    val duration: Duration,
    val liked: Boolean,
) {
    val artistName = artists.joinToString(" & ") { it.name }
}
