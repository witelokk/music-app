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

        playbackEngine.load(currentSong.toPlaybackItem())
        playbackEngine.play()

        publishState(
            snapshot = snapshot,
            playing = true,
            currentPosition = Duration.ZERO
        )
    }

    fun playSongInQueue(index: Int) {
        val snapshot = queueManager.playAt(index)
        val currentSong = snapshot.currentSong ?: return

        playbackEngine.load(currentSong.toPlaybackItem())
        playbackEngine.play()

        publishState(
            snapshot = snapshot,
            playing = true,
            currentPosition = Duration.ZERO
        )
    }

    fun seekToNext() {
        val snapshot = queueManager.moveToNext()
        val currentSong = snapshot.currentSong ?: return

        playbackEngine.load(currentSong.toPlaybackItem())
        playbackEngine.play()

        publishState(
            snapshot = snapshot,
            playing = true,
            currentPosition = Duration.ZERO
        )
    }

    fun seekToPrevious() {
        val snapshot = queueManager.moveToPrevious()
        val currentSong = snapshot.currentSong ?: return

        playbackEngine.load(currentSong.toPlaybackItem())
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
            val currentSong = result.snapshot.currentSong ?: return
            playbackEngine.load(currentSong.toPlaybackItem())

            publishState(
                snapshot = result.snapshot,
                playing = false,
                currentPosition = Duration.ZERO
            )
            return
        }

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
                playbackEngine.load(currentSong.toPlaybackItem())
                if (currentState?.playing == true) {
                    playbackEngine.play()
                }
                publishState(
                    snapshot = result.snapshot,
                    playing = currentState?.playing ?: false,
                    currentPosition = Duration.ZERO
                )
                return
            }
        }

        publishState(
            snapshot = result.snapshot,
            playing = currentState?.playing ?: false,
            currentPosition = currentState?.currentPosition ?: Duration.ZERO
        )
    }

    fun updateSong(song: Song) {
        val previous = _state.value
        val snapshot = queueManager.updateSong(song)

        if (snapshot.currentSong?.id == song.id) {
            playbackEngine.load(song.toPlaybackItem())
            if (previous?.playing == true) {
                playbackEngine.play()
            }
        }

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