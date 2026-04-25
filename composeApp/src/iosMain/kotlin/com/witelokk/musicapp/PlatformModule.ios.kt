package com.witelokk.musicapp

import androidx.room.RoomDatabase
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.cache.MusicAppDatabase
import com.witelokk.musicapp.cache.NoOpMediaCache
import com.witelokk.musicapp.cache.getRoomDatabaseBuilder
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule = module {
    single<HttpClientEngineFactory<*>> {
        Darwin
    }

    single {
        createDataStore(
            producePath = {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null
                )
                requireNotNull(documentDirectory).path + "/$DATASTORE_FILE_NAME"
            }
        )
    }

    single<RoomDatabase.Builder<MusicAppDatabase>> {
        getRoomDatabaseBuilder()
    }

    single<PlaybackEngine> {
        AvPlayerPlaybackEngine(get())
    }

    single<MediaCache> {
        NoOpMediaCache()
    }

    single<GoogleSignIn> {
        IosGoogleSignIn()
    }
}
