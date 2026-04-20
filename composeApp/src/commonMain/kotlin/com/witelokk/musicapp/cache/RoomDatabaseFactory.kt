package com.witelokk.musicapp.cache

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun getRoomDatabase(
    builder: RoomDatabase.Builder<MusicAppDatabase>,
): MusicAppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
