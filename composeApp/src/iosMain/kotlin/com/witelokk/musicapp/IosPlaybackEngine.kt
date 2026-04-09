package com.witelokk.musicapp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.MediaPlayer.MPChangePlaybackPositionCommandEvent
import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtwork
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandEvent
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIImage
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class IosPlaybackEngine(
    private val settingsRepository: SettingsRepository
) : PlaybackEngine {
    private var listener: PlaybackEngineListener? = null

    private var currentItem: PlaybackItem? = null
    private var player: AVPlayer? = null
    private var timeObserver: Any? = null
    private var artwork: MPMediaItemArtwork? = null

    private var playing = false
    private var positionMs = 0L
    private var remoteCommandsConfigured = false

    override val isPlaying: Boolean
        get() = playing

    override val currentPositionMs: Long
        get() = positionMs

    override fun load(item: PlaybackItem) {
        currentItem = item
        positionMs = 0L
        playing = false

        clearTimeObserver()

        player?.pause()
        player = null
        artwork = null

        configureAudioSession()
        configureRemoteCommands()

        val url = NSURL.URLWithString(item.url) ?: return
        val playerItem = createPlayerItem(url)
        val newPlayer = AVPlayer(playerItem)
        player = newPlayer

        artwork = loadArtwork(item.artworkUrl)

        attachTimeObserver(newPlayer)
        updateNowPlaying()

        listener?.onPositionChanged(0L)
        listener?.onIsPlayingChanged(false)
    }

    override fun play() {
        val p = player ?: return
        if (currentItem == null) return

        configureAudioSession()

        p.play()
        playing = true
        updatePositionFromPlayer()
        updateNowPlaying()
        listener?.onIsPlayingChanged(true)
    }

    override fun pause() {
        player?.pause()
        playing = false
        updatePositionFromPlayer()
        updateNowPlaying()
        listener?.onIsPlayingChanged(false)
    }

    override fun stop() {
        player?.pause()
        seekInternal(0L)
        playing = false
        updateNowPlaying()
        listener?.onIsPlayingChanged(false)
        listener?.onPositionChanged(0L)
    }

    override fun seekTo(positionMs: Long) {
        seekInternal(positionMs)
        updateNowPlaying()
        listener?.onPositionChanged(this.positionMs)
    }

    override fun setListener(listener: PlaybackEngineListener) {
        this.listener = listener
    }

    private fun createPlayerItem(url: NSURL): AVPlayerItem {
        val token = runBlocking { settingsRepository.accessToken.first() }

        if (token.isNotBlank()) {
            logd("IOS_PLAYBACK_ENGINE", "ACCESS TOKEN is $token")

            val options: Map<Any?, Any> = mapOf(
                "AVURLAssetHTTPHeaderFieldsKey" to mapOf(
                    "Authorization" to "Bearer $token"
                )
            )
            val asset = AVURLAsset(
                uRL = url,
                options = options
            )
            return AVPlayerItem(asset = asset)
        }

        return AVPlayerItem(uRL = url)
    }

    private fun seekInternal(targetMs: Long) {
        val p = player ?: return
        val safeMs = targetMs.coerceAtLeast(0L)
        positionMs = safeMs

        val time = CMTimeMakeWithSeconds(
            safeMs.toDouble() / 1000.0,
            preferredTimescale = 1000
        )
        p.seekToTime(time)
    }

    private fun updatePositionFromPlayer() {
        val p = player ?: return
        val seconds = CMTimeGetSeconds(p.currentTime())
        if (!seconds.isNaN() && !seconds.isInfinite() && seconds >= 0.0) {
            positionMs = (seconds * 1000.0).toLong()
            listener?.onPositionChanged(positionMs)
        }
    }

    private fun attachTimeObserver(player: AVPlayer) {
        clearTimeObserver()

        val interval = CMTimeMakeWithSeconds(0.5, preferredTimescale = 1000)

        timeObserver = player.addPeriodicTimeObserverForInterval(
            interval = interval,
            queue = dispatch_get_main_queue()
        ) { time ->
            val seconds = CMTimeGetSeconds(time)
            if (!seconds.isNaN() && !seconds.isInfinite() && seconds >= 0.0) {
                positionMs = (seconds * 1000.0).toLong()
                listener?.onPositionChanged(positionMs)

                if (playing) {
                    updateNowPlaying()
                }
            }
        }
    }

    private fun clearTimeObserver() {
        val p = player
        val observer = timeObserver
        if (p != null && observer != null) {
            p.removeTimeObserver(observer)
        }
        timeObserver = null
    }

    private fun configureAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }

    private fun configureRemoteCommands() {
        if (remoteCommandsConfigured) return
        remoteCommandsConfigured = true

        val center = MPRemoteCommandCenter.sharedCommandCenter()

        center.playCommand.addTargetWithHandler { _: MPRemoteCommandEvent? ->
            play()
            MPRemoteCommandHandlerStatusSuccess
        }

        center.pauseCommand.addTargetWithHandler { _: MPRemoteCommandEvent? ->
            pause()
            MPRemoteCommandHandlerStatusSuccess
        }

        center.changePlaybackPositionCommand.addTargetWithHandler { event: MPRemoteCommandEvent? ->
            val changeEvent = event as? MPChangePlaybackPositionCommandEvent
            val seconds = changeEvent?.positionTime ?: 0.0
            seekTo((seconds * 1000.0).toLong())
            MPRemoteCommandHandlerStatusSuccess
        }
    }

    private fun updateNowPlaying() {
        val item = currentItem ?: return

        val elapsedSeconds = player
            ?.let { CMTimeGetSeconds(it.currentTime()) }
            ?.takeIf { !it.isNaN() && !it.isInfinite() && it >= 0.0 }
            ?: (positionMs.toDouble() / 1000.0)

        val durationSeconds = player
            ?.currentItem
            ?.duration
            ?.let { CMTimeGetSeconds(it) }
            ?.takeIf { !it.isNaN() && !it.isInfinite() && it > 0.0 }

        val info = mutableMapOf<Any?, Any>(
            MPMediaItemPropertyTitle to item.title,
            MPMediaItemPropertyArtist to item.artist,
            MPNowPlayingInfoPropertyElapsedPlaybackTime to elapsedSeconds,
            MPNowPlayingInfoPropertyPlaybackRate to if (playing) 1.0 else 0.0
        )

        durationSeconds?.let {
            info[MPMediaItemPropertyPlaybackDuration] = it
        }

        artwork?.let {
            info[MPMediaItemPropertyArtwork] = it
        }

        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
    }

    private fun loadArtwork(url: String?): MPMediaItemArtwork? {
        if (url.isNullOrBlank()) return null

        val nsUrl = NSURL.URLWithString(url) ?: return null
        val data = NSData.dataWithContentsOfURL(nsUrl) ?: return null
        val image = UIImage(data = data) ?: return null

        return MPMediaItemArtwork(
            boundsSize = image.size,
            requestHandler = { _ -> image }
        )
    }
}
