package com.witelokk.musicapp

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import coil3.ImageLoader
import coil3.toBitmap
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(UnstableApi::class)
class Media3PlaybackEngine(
    private val context: Context,
    private val mediaControllerFuture: ListenableFuture<MediaController>,
    private val imageLoader: ImageLoader,
) : PlaybackEngine {
    private lateinit var controller: MediaController
    private var listener: PlaybackEngineListener? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playlistItems: List<PlaybackItem> = emptyList()

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

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentIndex = controller.currentMediaItemIndex
                if (currentIndex !in playlistItems.indices) return

                loadArtwork(playlistItems[currentIndex], ++artworkRequestId)
                listener?.onCurrentItemChanged(currentIndex)
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

    override fun loadQueue(items: List<PlaybackItem>, startIndex: Int, startPositionMs: Long) {
        playlistItems = items

        if (items.isEmpty()) {
            controller.stop()
            return
        }

        val safeIndex = startIndex.coerceIn(items.indices)
        val requestId = ++artworkRequestId

        controller.setMediaItems(items.map { it.toMediaItem() }, safeIndex, startPositionMs)
        controller.prepare()

        loadArtwork(items[safeIndex], requestId)
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

    override fun seekToQueueItem(index: Int, positionMs: Long) {
        if (index !in playlistItems.indices) return
        controller.seekTo(index, positionMs)
    }

    override fun seekToNextItem() {
        if (!controller.hasNextMediaItem()) return
        controller.seekToNextMediaItem()
    }

    override fun seekToPreviousItem() {
        if (!controller.hasPreviousMediaItem()) return
        controller.seekToPreviousMediaItem()
    }

    override fun setListener(listener: PlaybackEngineListener) {
        this.listener = listener
    }

    private fun loadArtwork(item: PlaybackItem, requestId: Int) {
        val artworkUrl = item.artworkUrl ?: return

        scope.launch {
            val artworkBytes = runCatching {
                val result = imageLoader.execute(
                    ImageRequest.Builder(context)
                        .data(artworkUrl)
                        .build()
                )
                (result as? SuccessResult)
                    ?.image
                    ?.let { image -> image.toBitmap(image.width, image.height) }
                    ?.toByteArray()
            }.getOrElse { error ->
                loge(
                    "ANDROID_PLAYBACK_ENGINE",
                    "Failed to load artwork for $artworkUrl: ${error.message}"
                )
                null
            } ?: return@launch

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

    private fun Bitmap.toByteArray(): ByteArray {
        return ByteArrayOutputStream().use { output ->
            compress(Bitmap.CompressFormat.PNG, 100, output)
            output.toByteArray()
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
