package com.witelokk.musicapp

interface PlaybackEngine {
    val isPlaying: Boolean
    val currentPositionMs: Long

    fun load(item: PlaybackItem)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)

    fun setListener(listener: PlaybackEngineListener)
}

interface PlaybackEngineListener {
    fun onIsPlayingChanged(isPlaying: Boolean) {}
    fun onPositionChanged(positionMs: Long) {}
    fun onPlaybackEnded() {}
}

data class PlaybackItem(
    val id: String,
    val url: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
)