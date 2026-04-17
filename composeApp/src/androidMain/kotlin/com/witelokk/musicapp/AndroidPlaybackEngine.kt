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
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@OptIn(UnstableApi::class)
class AndroidPlaybackEngine(
    private val mediaControllerFuture: ListenableFuture<MediaController>,
    private val settingsRepository: SettingsRepository,
) : PlaybackEngine {

    private lateinit var controller: MediaController
    private var listener: PlaybackEngineListener? = null
    @Volatile
    private var artworkRequestId = 0

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
        val requestId = ++artworkRequestId
        controller.setMediaItem(item.toMediaItem())
        controller.prepare()
        controller.seekTo(0)

        loadArtwork(item, requestId)
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

    private fun loadArtwork(item: PlaybackItem, requestId: Int) {
        val artworkUrl = item.artworkUrl ?: return

        thread(name = "artwork-loader-${item.id}") {
            val artworkBytes = runCatching {
                val connection = (URL(artworkUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    val token = runBlocking { settingsRepository.accessToken.first() }
                    if (token.isNotBlank()) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                }

                try {
                    connection.inputStream.use { input -> input.readBytes() }
                } finally {
                    connection.disconnect()
                }
            }.getOrElse { error ->
                loge("ANDROID_PLAYBACK_ENGINE", "Failed to load artwork for $artworkUrl: ${error.message}")
                null
            } ?: return@thread

            Handler(Looper.getMainLooper()).post {
                if (!::controller.isInitialized || requestId != artworkRequestId) return@post
                val currentIndex = controller.currentMediaItemIndex
                if (currentIndex == -1) return@post

                controller.replaceMediaItem(
                    currentIndex,
                    item.toMediaItem(artworkBytes)
                )
            }
        }
    }

    private fun PlaybackItem.toMediaItem(artworkData: ByteArray? = null): MediaItem {
        return MediaItem.Builder()
            .setMediaId(url)
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setDisplayTitle(title)
                    .setArtist(artist)
                    .setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    .build()
            )
            .build()
    }
}
