package com.witelokk.musicapp

import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MusicPlayer(
    private val playbackEngine: PlaybackEngine,
    private val queueManager: PlaybackQueueManager = PlaybackQueueManager(),
){

    private val _state = MutableStateFlow<PlayerState?>(null)
    val state: StateFlow<PlayerState?> = _state.asStateFlow()

    init {
        playbackEngine.setListener(
            object : PlaybackEngineListener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it?.copy(playing = isPlaying) }
                }

                override fun onPositionChanged(positionMs: Long) {
                    _state.update {
                        it?.copy(
                            currentPosition = positionMs.milliseconds,
                            playing = playbackEngine.isPlaying
                        )
                    }
                }

                override fun onPlaybackEnded() {
                    val current = _state.value ?: return

                    if (current.nextTrackAvailable) {
                        seekToNext()
                    } else {
                        _state.update { it?.copy(playing = false) }
                    }
                }

                override fun onCurrentItemChanged(index: Int) {
                    if (index < 0) return

                    val snapshot = queueManager.onCurrentItemChanged(index)
                    publishState(
                        snapshot = snapshot,
                        playing = playbackEngine.isPlaying,
                        currentPosition = playbackEngine.currentPositionMs.milliseconds
                    )
                }
            }
        )
    }

    fun playPause() {
        val state = _state.value ?: return

        if (state.playing) {
            playbackEngine.pause()
        } else {
            playbackEngine.play()
        }
    }

    fun seek(to: Duration) {
        playbackEngine.seekTo(to.inWholeMilliseconds)
        _state.update { it?.copy(currentPosition = to) }
    }

    fun setQueueAndPlay(songs: List<Song>, index: Int) {
        val snapshot = queueManager.setQueue(songs, index)
        val currentSong = snapshot.currentSong ?: run {
            playbackEngine.stop()
            _state.value = null
            return
        }

        playbackEngine.loadQueue(
            items = snapshot.queue.map { it.toPlaybackItem() },
            startIndex = snapshot.currentSongIndex
        )
        playbackEngine.play()

        publishState(
            snapshot = snapshot,
            playing = true,
            currentPosition = Duration.ZERO
        )
    }

    fun playSongInQueue(index: Int) {
        val snapshot = queueManager.playAt(index)
        snapshot.currentSong ?: return

        playbackEngine.seekToQueueItem(snapshot.currentSongIndex)
        playbackEngine.play()

        publishState(
            snapshot = snapshot,
            playing = true,
            currentPosition = Duration.ZERO
        )
    }

    fun seekToNext() {
        val snapshot = queueManager.moveToNext()
        snapshot.currentSong ?: return

        playbackEngine.seekToNextItem()
        playbackEngine.play()

        publishState(
            snapshot = snapshot,
            playing = true,
            currentPosition = Duration.ZERO
        )
    }

    fun seekToPrevious() {
        val snapshot = queueManager.moveToPrevious()
        snapshot.currentSong ?: return

        playbackEngine.seekToPreviousItem()
        playbackEngine.play()

        publishState(
            snapshot = snapshot,
            playing = true,
            currentPosition = Duration.ZERO
        )
    }

    fun addToQueue(song: Song) {
        val result = queueManager.addNext(song)

        if (result.shouldStartNewQueue) {
            result.snapshot.currentSong ?: return
            playbackEngine.loadQueue(
                items = result.snapshot.queue.map { it.toPlaybackItem() },
                startIndex = result.snapshot.currentSongIndex
            )

            publishState(
                snapshot = result.snapshot,
                playing = false,
                currentPosition = Duration.ZERO
            )
            return
        }

        syncPlaybackQueue(
            snapshot = result.snapshot,
            playing = _state.value?.playing ?: false,
            currentPosition = _state.value?.currentPosition ?: Duration.ZERO
        )
        publishState(result.snapshot)
    }

    fun removeFromQueue(index: Int) {
        val currentState = _state.value
        val removingCurrent = currentState?.currentSongIndex == index

        val result = queueManager.removeAt(index)

        if (result.queueBecameEmpty) {
            playbackEngine.stop()
            _state.value = null
            return
        }

        if (removingCurrent) {
            val currentSong = result.snapshot.currentSong
            if (currentSong != null) {
                syncPlaybackQueue(
                    snapshot = result.snapshot,
                    playing = currentState.playing,
                    currentPosition = Duration.ZERO
                )
                publishState(
                    snapshot = result.snapshot,
                    playing = currentState.playing,
                    currentPosition = Duration.ZERO
                )
                return
            }
        }

        syncPlaybackQueue(
            snapshot = result.snapshot,
            playing = currentState?.playing ?: false,
            currentPosition = currentState?.currentPosition ?: Duration.ZERO
        )

        publishState(
            snapshot = result.snapshot,
            playing = currentState?.playing ?: false,
            currentPosition = currentState?.currentPosition ?: Duration.ZERO
        )
    }

    fun updateSong(song: Song) {
        val previous = _state.value
        val snapshot = queueManager.updateSong(song)

        syncPlaybackQueue(
            snapshot = snapshot,
            playing = previous?.playing ?: false,
            currentPosition = previous?.currentPosition ?: Duration.ZERO
        )

        publishState(
            snapshot = snapshot,
            playing = previous?.playing ?: false,
            currentPosition = previous?.currentPosition ?: Duration.ZERO
        )
    }

    private fun publishState(
        snapshot: QueueSnapshot,
        playing: Boolean = playbackEngine.isPlaying,
        currentPosition: Duration = _state.value?.currentPosition ?: 0.seconds,
    ) {
        val currentSong = snapshot.currentSong ?: run {
            _state.value = null
            return
        }

        _state.value = PlayerState(
            currentSong = currentSong,
            currentSongIndex = snapshot.currentSongIndex,
            playing = playing,
            currentPosition = currentPosition,
            previousTrackAvailable = snapshot.previousTrackAvailable,
            nextTrackAvailable = snapshot.nextTrackAvailable,
            queue = snapshot.queue,
        )
    }

    private fun syncPlaybackQueue(
        snapshot: QueueSnapshot,
        playing: Boolean,
        currentPosition: Duration,
    ) {
        if (snapshot.currentSong == null) return

        playbackEngine.loadQueue(
            items = snapshot.queue.map { it.toPlaybackItem() },
            startIndex = snapshot.currentSongIndex,
            startPositionMs = currentPosition.inWholeMilliseconds
        )

        if (playing) {
            playbackEngine.play()
        }
    }
}

private fun Song.toPlaybackItem(): PlaybackItem {
    return PlaybackItem(
        id = id,
        url = streamUrl,
        title = name,
        artist = artists.joinToString(", ") { it.name },
        artworkUrl = coverUrl
    )
}
