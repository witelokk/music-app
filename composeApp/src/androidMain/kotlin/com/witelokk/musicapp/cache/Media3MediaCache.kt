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
    private val cacheStates = mutableMapOf<String, MutableStateFlow<Boolean>>()

    private val listener = object : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            val url = download.request.id
            val isCached = download.state == Download.STATE_COMPLETED

            cacheStates[url]?.value = isCached
        }
    }

    init {
        downloadManager.addListener(listener)
    }

    override fun cache(url: String) {
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

    override fun isCached(url: String): StateFlow<Boolean> {
        return cacheStates.getOrPut(url) {
            MutableStateFlow(checkInitialState(url))
        }
    }

    private fun checkInitialState(url: String): Boolean {
        val download = downloadManager.downloadIndex.getDownload(url)
        return download != null && download.state == Download.STATE_COMPLETED
    }
}
