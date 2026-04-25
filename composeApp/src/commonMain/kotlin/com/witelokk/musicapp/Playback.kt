package com.witelokk.musicapp

interface PlaybackEngine {
    val isPlaying: Boolean
    val currentPositionMs: Long

    fun loadQueue(items: List<PlaybackItem>, startIndex: Int, startPositionMs: Long = 0L)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun seekToQueueItem(index: Int, positionMs: Long = 0L)
    fun seekToNextItem()
    fun seekToPreviousItem()

    fun setListener(listener: PlaybackEngineListener)
}

interface PlaybackEngineListener {
    fun onIsPlayingChanged(isPlaying: Boolean) {}
    fun onPositionChanged(positionMs: Long) {}
    fun onPlaybackEnded() {}
    fun onCurrentItemChanged(index: Int) {}
}

data class PlaybackItem(
    val id: String,
    val url: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
)
