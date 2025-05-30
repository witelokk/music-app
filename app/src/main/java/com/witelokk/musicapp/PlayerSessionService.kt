package com.witelokk.musicapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import org.koin.android.ext.android.inject


class PlayerSessionService : MediaSessionService(), MediaSession.Callback {
    companion object {
        private var mediaSessionInstance: MediaSession? = null

        class Enums {
            companion object {
                enum class NotificationPlayerCustomCommandButton(
                    val commandButton: CommandButton,
                )
            }
        }
    }

    private val player by inject<ExoPlayer>()
    private var mediaSession: MediaSession? = null
    private var currentSongPosition: Long = 0L
    private var isPauseFromLoss = false

    private var audioManager: AudioManager? = null
    private var audioFocusState: Int = AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    private var focusRequest: AudioFocusRequest? = null
    private val attributes = AudioAttributes.Builder().apply {
        setUsage(AudioAttributes.USAGE_MEDIA)
        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
    }.build()

    private val notificationPlayerCustomCommandButtons =
        Enums.Companion.NotificationPlayerCustomCommandButton.entries.map { command -> command.commandButton }

    override fun onCreate() {
        super.onCreate()
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initializeSessionAndPlayer()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun releaseAudioFocus() {
        focusRequest?.let {
            if (Build.VERSION.SDK_INT >= 26)
                audioManager?.abandonAudioFocusRequest(it)
            else
                audioManager?.abandonAudioFocus(focusChangeListener)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setupAndRequestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            focusRequest?.let {
                audioFocusState =
                    audioManager?.requestAudioFocus(it) ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
            }
        } else {
            audioFocusState = audioManager?.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }
    }

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { state ->
        audioFocusState = state
        when (state) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (isPauseFromLoss) {
                    player.play()
                    isPauseFromLoss = false
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (player.isPlaying) {
                    player.pause()
                    isPauseFromLoss = true
                }
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                if (player.isPlaying) {
                    player.pause()
                }
            }
        }
    }

    private fun initializeSessionAndPlayer() {
        if (mediaSessionInstance == null) {
            mediaSessionInstance = MediaSession.Builder(this, player).setCallback(this).build()
        }
        mediaSession = mediaSessionInstance

        mediaSession?.setCustomLayout(notificationPlayerCustomCommandButtons)
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        val updatedMediaItems =
            mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
        return Futures.immediateFuture(updatedMediaItems)
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()

        notificationPlayerCustomCommandButtons.forEach { commandButton ->
            commandButton.sessionCommand?.let(availableSessionCommands::add)
        }

        return MediaSession.ConnectionResult.accept(
            availableSessionCommands.build(),
            connectionResult.availablePlayerCommands
        )
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        super.onPostConnect(session, controller)
        if (notificationPlayerCustomCommandButtons.isNotEmpty()) {
            mediaSession?.setCustomLayout(notificationPlayerCustomCommandButtons)
            if (player.playWhenReady)
                setupAndRequestAudioFocus()
        }
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        currentSongPosition = session.player.currentPosition

        if (player.playWhenReady)
            setupAndRequestAudioFocus()

        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            release()
            player.release()
            mediaSession = null
            mediaSessionInstance = null
        }
        releaseAudioFocus()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mediaSession?.player?.let { player ->
            if (player.playWhenReady) {
                player.pause()
            }
            stopSelf()
        }
        releaseAudioFocus()
    }
}
