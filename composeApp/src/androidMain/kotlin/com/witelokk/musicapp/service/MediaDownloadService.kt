package com.witelokk.musicapp.service

import android.app.Notification
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.witelokk.musicapp.R
import org.koin.android.ext.android.inject

@UnstableApi
class MediaDownloadService(
) : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    "download_channel",
    R.string.downloads_notification_channel_name,
    0
) {
    private val _downloadManager by inject<DownloadManager>()

    override fun getDownloadManager(): DownloadManager {
        return _downloadManager
    }

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return DownloadNotificationHelper(this, "download_channel")
            .buildProgressNotification(
                this,
                R.drawable.ic_download,
                null,
                "Downloading media",
                downloads,
                notMetRequirements
            )
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }
}