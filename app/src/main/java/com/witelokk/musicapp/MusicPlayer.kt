package com.witelokk.musicapp

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
class MusicPlayer
    (
    private val mediaControllerFuture: ListenableFuture<MediaController>
) {
    private val _state = MutableStateFlow<PlayerState?>(null)
    val state = _state.asStateFlow()

    private lateinit var controller: MediaController
    private var queue = arrayListOf<Song>()

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        mediaControllerFuture.apply {
            addListener({
                controller = get()
                configureMediaController()
            }, MoreExecutors.directExecutor())
        }
    }

    private fun configureMediaController() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val position = controller.currentPosition
                onCurrentPositionChanged(position)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)

        controller.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    onSongEnded()
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                Log.d("onMediaMetadataChanged", mediaMetadata.displayTitle.toString())
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                this@MusicPlayer.onIsPlayingChanged(isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                onMediaItemChanged(mediaItem)
            }
        })
    }

    private fun onMediaItemChanged(mediaItem: MediaItem?) {
        queue.find { it.streamUrl == mediaItem?.mediaId }?.let { song ->
            _state.update {
                PlayerState(
                    currentSong = song,
                    currentSongIndex = queue.indexOf(song),
                    playing = false,
                    currentPosition = 0.seconds,
                    previousTrackAvailable = false,
                    nextTrackAvailable = false,
                    queue = queue,
                )
            }
        }
    }

    private fun onCurrentPositionChanged(currentPosition: Long) {
        _state.update {
            it?.copy(
                currentPosition = currentPosition.milliseconds,
                playing = controller.isPlaying,
            )
        }
    }

    private fun onSongEnded() {
        _state.update {
            it?.copy(
                playing = false
            )
        }
    }

    private fun onIsPlayingChanged(isPlaying: Boolean) {
        _state.update {
            it?.copy(
                playing = isPlaying
            )
        }
    }

    private fun createMediaItem(song: Song): MediaItem {
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtworkUri(Uri.parse(song.coverUrl))
                    .setDisplayTitle(song.name)
                    .setTitle(song.name)
                    .setArtist(song.artists.joinToString(", ") { it.name })
                    .build()
            )
            .build()
        return mediaItem
    }

    fun playPause() {
        _state.update { it?.copy(playing = !it.playing) }

        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seek(to: Duration) {
        _state.update { it?.copy(currentPosition = to) }

        controller.seekTo(to.inWholeMilliseconds)
    }

    fun setQueueAndPlay(songs: List<Song>, index: Int) {
        controller.stop()

        queue.clear()
        queue.addAll(songs)
        controller.setMediaItems(songs.map { createMediaItem(it) })

        _state.update { it?.copy(queue = songs) }

        controller.play()

        controller.seekTo(index, 0)
    }

    fun playSongInQueue(index: Int) {
        controller.seekTo(index, 0)
    }

    fun seekToNext() {
        controller.seekToNextMediaItem()
        controller.play()
    }

    fun seekToPrevious() {
        controller.seekToPrevious()
    }

    fun addToQueue(song: Song) {
        if (queue.isEmpty()) {
            queue.add(song)
            controller.addMediaItem(createMediaItem(song))
            controller.prepare()
        }

        if (song in queue) {
            removeFromQueue(queue.indexOf(song))
        }

        val currentSongIndex = controller.currentMediaItemIndex
        if (queue.isEmpty()) {
            setQueueAndPlay(listOf(song), 0)
            controller.pause()
        } else {
            queue.add(currentSongIndex + 1, song)
        }
        controller.addMediaItem(currentSongIndex + 1, createMediaItem(song))
    }

    fun removeFromQueue(index: Int) {
        if (index == 0 && queue.size == 1) {
            controller.stop()
            queue.clear()
            controller.clearMediaItems()
            _state.update { null }
            return
        }

        controller.removeMediaItem(index)
        val newQueue = arrayListOf<Song>()
        queue.forEachIndexed { i, song ->
            if (i == index)
                return@forEachIndexed
            newQueue.add(song)
        }
        queue = newQueue
        _state.update { it?.copy(queue = newQueue) }
    }

    fun updateSong(song: Song) {
        for (i in 0..<queue.size) {
            if (queue[i].id == song.id) {
                queue[i] = song
            }
        }

        _state.update {
            it?.copy(
                currentSong = if (it.currentSong.id == song.id) song else it.currentSong,
                queue = queue,
            )
        }
    }
}