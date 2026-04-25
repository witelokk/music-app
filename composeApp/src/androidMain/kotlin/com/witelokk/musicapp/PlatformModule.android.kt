package com.witelokk.musicapp

import android.content.ComponentName
import android.content.Context
import androidx.room.RoomDatabase
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.witelokk.musicapp.cache.AndroidMediaCache
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.cache.MusicAppDatabase
import com.witelokk.musicapp.cache.getRoomDatabaseBuilder
import com.witelokk.musicapp.service.PlayerSessionService
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.Executors

actual val platformModule = module {
    single<HttpClientEngineFactory<*>> {
        OkHttp
    }

    single {
        createDataStore(
            producePath = { get<Context>().filesDir.resolve(DATASTORE_FILE_NAME).absolutePath }
        )
    }

    single<RoomDatabase.Builder<MusicAppDatabase>> {
        getRoomDatabaseBuilder(get())
    }

    single {
        val sessionToken =
            SessionToken(get(), ComponentName(get(), PlayerSessionService::class.java))
        MediaController.Builder(get(), sessionToken).buildAsync()
    }

    single {
        AndroidPlaybackHttpFactory(get())
    }

    @UnstableApi
    single<Cache> {
        val cache = SimpleCache(
            File(get<Context>().filesDir, "media"),
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(get())
        )
        cache
    }

    @UnstableApi
    single {
        val downloadManager = DownloadManager(
            get(),
            StandaloneDatabaseProvider(get()),
            get(),
            get<AndroidPlaybackHttpFactory>().factory,
            Executors.newFixedThreadPool(3)
        )
        downloadManager
    }

    @UnstableApi
    single {
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(get())
            .setUpstreamDataSourceFactory(get<AndroidPlaybackHttpFactory>().factory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val factory = HlsMediaSource.Factory(cacheDataSourceFactory)
        ExoPlayer.Builder(get()).setMediaSourceFactory(factory).build()
    }

    single {
        MediaSession.Builder(get(), get<ExoPlayer>()).build()
    }

    single<PlaybackEngine> {
        AndroidPlaybackEngine(get(), get())
    }

    @UnstableApi
    single<MediaCache> {
        AndroidMediaCache(get(), get())
    }

    single<GoogleSignIn> {
        AndroidGoogleSignIn(get())
    }
}
