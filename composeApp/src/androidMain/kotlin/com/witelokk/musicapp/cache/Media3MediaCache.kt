package com.witelokk.musicapp.cache

import android.content.Context
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import com.witelokk.musicapp.service.MediaDownloadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@UnstableApi
class Media3MediaCache(
    private val context: Context,
    private val downloadManager: DownloadManager,
) : MediaCache {
    private val cacheStates = mutableMapOf<String, MutableStateFlow<MediaCacheState>>()

    private val listener = object : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            val url = download.request.id
            cacheStates[url]?.value = download.toCacheState()
        }
    }

    init {
        downloadManager.addListener(listener)
    }

    override fun cache(url: String) {
        val existingState = cacheStates[url]?.value ?: checkInitialState(url)
        if (existingState != MediaCacheState.CACHED) {
            cacheStates.getOrPut(url) { MutableStateFlow(existingState) }.value = MediaCacheState.IN_PROGRESS
        }

        val request = DownloadRequest.Builder(url, url.toUri())
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        DownloadService.sendAddDownload(
            context,
            MediaDownloadService::class.java,
            request,
            false
        )
    }

    override fun getCacheState(url: String): StateFlow<MediaCacheState> {
        return cacheStates.getOrPut(url) {
            MutableStateFlow(checkInitialState(url))
        }
    }

    private fun checkInitialState(url: String): MediaCacheState {
        val download = downloadManager.downloadIndex.getDownload(url)
        return download?.toCacheState() ?: MediaCacheState.NOT_CACHED
    }

    private fun Download.toCacheState(): MediaCacheState {
        return when (state) {
            Download.STATE_COMPLETED -> MediaCacheState.CACHED
            Download.STATE_FAILED -> MediaCacheState.FAILED
            Download.STATE_QUEUED,
            Download.STATE_STOPPED,
            Download.STATE_DOWNLOADING,
            Download.STATE_RESTARTING -> MediaCacheState.IN_PROGRESS
            Download.STATE_REMOVING -> MediaCacheState.NOT_CACHED
            else -> MediaCacheState.NOT_CACHED
        }
    }
}
