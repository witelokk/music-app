package com.witelokk.musicapp

import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

    single<PlaybackEngine> {
        IosPlaybackEngine()
    }

    single<GoogleSignIn> {
        object : GoogleSignIn {
            override suspend fun signIn(
                signIn: (String) -> Unit,
                onSingInFailed: () -> Unit
            ) {
            }
        }
    }
}
