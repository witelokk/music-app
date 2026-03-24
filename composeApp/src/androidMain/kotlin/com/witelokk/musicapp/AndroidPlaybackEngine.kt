package com.witelokk.musicapp

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

@OptIn(UnstableApi::class)
class AndroidPlaybackEngine(
    private val mediaControllerFuture: ListenableFuture<MediaController>
) : PlaybackEngine {

    private lateinit var controller: MediaController
    private var listener: PlaybackEngineListener? = null

    init {
        mediaControllerFuture.addListener(
            {
                controller = mediaControllerFuture.get()
                configure()
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun configure() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (::controller.isInitialized) {
                    listener?.onPositionChanged(controller.currentPosition)
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(runnable)

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                listener?.onIsPlayingChanged(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    listener?.onPlaybackEnded()
                }
            }
        })
    }

    override val isPlaying: Boolean
        get() = ::controller.isInitialized && controller.isPlaying

    override val currentPositionMs: Long
        get() = if (::controller.isInitialized) controller.currentPosition else 0L

    override fun load(item: PlaybackItem) {
        controller.setMediaItem(item.toMediaItem())
        controller.prepare()
        controller.seekTo(0)
    }

    override fun play() {
        controller.play()
    }

    override fun pause() {
        controller.pause()
    }

    override fun stop() {
        controller.stop()
    }

    override fun seekTo(positionMs: Long) {
        controller.seekTo(positionMs)
    }

    override fun setListener(listener: PlaybackEngineListener) {
        this.listener = listener
    }

    private fun PlaybackItem.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(url)
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setDisplayTitle(title)
                    .setArtist(artist)
                    .setArtworkUri(artworkUrl?.let(Uri::parse))
                    .build()
            )
            .build()
    }
}