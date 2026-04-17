package com.witelokk.musicapp

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngineFactory<*>> {
        OkHttp
    }

    single {
        createDataStore(
            producePath = { get<Context>().filesDir.resolve(DATASTORE_FILE_NAME).absolutePath }
        )
    }

    single {
        val sessionToken =
            SessionToken(get(), ComponentName(get(), PlayerSessionService::class.java))
        MediaController.Builder(get(), sessionToken).buildAsync()
    }

    single {
        @OptIn(UnstableApi::class)
        val dataSourceFactory = DefaultHttpDataSource.Factory().apply {
            setDefaultRequestProperties(
                mapOf(
                    "Authorization" to "Bearer " + (runBlocking {get<SettingsRepository>().accessToken.first()})
                )
            )
        }
        @OptIn(UnstableApi::class)
        val factory = HlsMediaSource.Factory(dataSourceFactory)
        ExoPlayer.Builder(get()).setMediaSourceFactory(factory).build()
    }

    single {
        MediaSession.Builder(get(), get<ExoPlayer>()).build()
    }

    single<PlaybackEngine> {
        AndroidPlaybackEngine(get(), get())
    }

    single<GoogleSignIn> {
        AndroidGoogleSignIn(get())
    }
}
