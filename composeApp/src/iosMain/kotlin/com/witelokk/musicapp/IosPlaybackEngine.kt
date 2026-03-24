package com.witelokk.musicapp

class IosPlaybackEngine : PlaybackEngine {
    private var listener: PlaybackEngineListener? = null

    private var currentItem: PlaybackItem? = null
    private var playing = false
    private var positionMs = 0L

    override val isPlaying: Boolean
        get() = playing

    override val currentPositionMs: Long
        get() = positionMs

    override fun load(item: PlaybackItem) {
        currentItem = item
        positionMs = 0L
        playing = false
        listener?.onPositionChanged(0L)
        listener?.onIsPlayingChanged(false)
    }

    override fun play() {
        if (currentItem == null) return
        playing = true
        listener?.onIsPlayingChanged(true)
    }

    override fun pause() {
        playing = false
        listener?.onIsPlayingChanged(false)
    }

    override fun stop() {
        playing = false
        positionMs = 0L
        listener?.onIsPlayingChanged(false)
        listener?.onPositionChanged(0L)
    }

    override fun seekTo(positionMs: Long) {
        this.positionMs = positionMs
        listener?.onPositionChanged(positionMs)
    }

    override fun setListener(listener: PlaybackEngineListener) {
        this.listener = listener
    }
}