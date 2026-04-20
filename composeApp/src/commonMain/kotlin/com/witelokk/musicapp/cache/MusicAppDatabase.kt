package com.witelokk.musicapp.cache

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [FavoriteSongEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(MusicAppDatabaseConstructor::class)
abstract class MusicAppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}

@Suppress("KotlinNoActualForExpect")
expect object MusicAppDatabaseConstructor : RoomDatabaseConstructor<MusicAppDatabase> {
    override fun initialize(): MusicAppDatabase
}
