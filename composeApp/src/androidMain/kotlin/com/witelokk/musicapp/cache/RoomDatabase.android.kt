package com.witelokk.musicapp.cache

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getRoomDatabaseBuilder(context: Context): RoomDatabase.Builder<MusicAppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.cacheDir.resolve("music-app.db")

    return Room.databaseBuilder<MusicAppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
