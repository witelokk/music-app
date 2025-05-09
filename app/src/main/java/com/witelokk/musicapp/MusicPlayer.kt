package com.witelokk.musicapp

import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.hls.HlsMediaSource
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
    private val exoPlayer: ExoPlayer,
    private val hlsMediaSourceFactory: HlsMediaSource.Factory,
) {
    private val _state = MutableStateFlow<PlayerState?>(null)
    val state = _state.asStateFlow()

    init {
        exoPlayer.playWhenReady = true

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    onSongEnded()
                }
            }
        })

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val position = exoPlayer.currentPosition
                onCurrentPositionChanged(position)
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)

    }

    private fun onCurrentPositionChanged(currentPosition: Long) {
        _state.update {
            it?.copy(
                currentPosition = currentPosition.milliseconds
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

    fun playPause() {
        _state.update { it?.copy(playing = !it.playing) }

        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seek(to: Duration) {
        _state.update { it?.copy(currentPosition = to, playing = true) }

        exoPlayer.seekTo(to.inWholeMilliseconds)
    }

    fun playSong(song: Song) {
        _state.update {
            PlayerState(
                song = song,
                playing = true,
                currentPosition = 0.seconds,
                previousTrackAvailable = false,
                nextTrackAvailable = false,
            )
        }

        exoPlayer.setMediaSource(hlsMediaSourceFactory.createMediaSource(MediaItem.fromUri(song.streamUrl)))
        exoPlayer.prepare()
        exoPlayer.play()
    }
}