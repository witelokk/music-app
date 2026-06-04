package com.witelokk.musicapp.cache

import com.witelokk.musicapp.auth.AuthStore
import com.witelokk.musicapp.loge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

interface IosMediaDownloadBridge {
    fun startDownload(url: String, token: String?)
    fun cachedLocation(url: String): String?
    fun cacheState(url: String): String
    fun setListener(listener: IosMediaDownloadBridgeListener?)
}

interface IosMediaDownloadBridgeListener {
    fun onDownloadStateChanged(url: String, state: String, location: String?)
}

object IosMediaDownloadBridgeRegistry {
    private var registeredBridge: IosMediaDownloadBridge? = null

    fun register(bridge: IosMediaDownloadBridge) {
        registeredBridge = bridge
    }

    fun bridge(): IosMediaDownloadBridge? = registeredBridge
}

class IosMediaCache(
    private val authStore: AuthStore,
) : MediaCache, IosMediaDownloadBridgeListener {
    private val cacheStates = mutableMapOf<String, MutableStateFlow<MediaCacheState>>()

    private val bridge: IosMediaDownloadBridge?
        get() = IosMediaDownloadBridgeRegistry.bridge()

    init {
        bridge?.setListener(this)
    }

    override fun cache(url: String) {
        val state = cacheStates.getOrPut(url) {
            MutableStateFlow(checkInitialState(url))
        }

        if (state.value == MediaCacheState.CACHED || state.value == MediaCacheState.IN_PROGRESS) {
            return
        }

        val downloader = bridge ?: run {
            loge("IOS_MEDIA_CACHE", "Native media download bridge is not registered")
            state.value = MediaCacheState.FAILED
            return
        }

        state.value = MediaCacheState.IN_PROGRESS
        loge("IOS_MEDIA_CACHE", "starting native AVAsset download url=$url")
        downloader.startDownload(url, authStore.currentAccessToken.takeIf { it.isNotBlank() })
    }

    override fun getCacheState(url: String): StateFlow<MediaCacheState> {
        return cacheStates.getOrPut(url) {
            MutableStateFlow(checkInitialState(url))
        }
    }

    fun cachedPlaybackUrl(url: String): NSURL? {
        val location = bridge?.cachedLocation(url) ?: return null
        return if (FileSystem.SYSTEM.exists(location.toPath())) {
            NSURL.fileURLWithPath(location)
        } else {
            cacheStates[url]?.value = MediaCacheState.NOT_CACHED
            null
        }
    }

    override fun onDownloadStateChanged(url: String, state: String, location: String?) {
        val flow = cacheStates.getOrPut(url) { MutableStateFlow(MediaCacheState.NOT_CACHED) }
        flow.value = state.toMediaCacheState(location)
        loge("IOS_MEDIA_CACHE", "native download state url=$url state=$state location=$location")
    }

    private fun checkInitialState(url: String): MediaCacheState {
        val downloader = bridge ?: return MediaCacheState.NOT_CACHED
        return downloader.cacheState(url).toMediaCacheState(downloader.cachedLocation(url))
    }

    private fun String.toMediaCacheState(location: String?): MediaCacheState {
        return when (this) {
            "cached" -> if (location != null && NSFileManager.defaultManager.fileExistsAtPath(location)) {
                MediaCacheState.CACHED
            } else {
                MediaCacheState.NOT_CACHED
            }

            "in_progress" -> MediaCacheState.IN_PROGRESS
            "failed" -> MediaCacheState.FAILED
            else -> MediaCacheState.NOT_CACHED
        }
    }
}
