package com.witelokk.musicapp.cache

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

fun getRoomDatabaseBuilder(): RoomDatabase.Builder<MusicAppDatabase> {
    val dbFilePath = cachesDirectory() + "/music-app.db"

    return Room.databaseBuilder<MusicAppDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun cachesDirectory(): String {
    val cachesDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )

    return requireNotNull(cachesDirectory?.path)
}
