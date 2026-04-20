package com.witelokk.musicapp.cache

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.AutoMigration

@Database(
    entities = [FavoriteSongEntity::class, HomeFeedEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true,
)
@ConstructedBy(MusicAppDatabaseConstructor::class)
abstract class MusicAppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun homeFeedDao(): HomeFeedDao
}

@Suppress("KotlinNoActualForExpect")
expect object MusicAppDatabaseConstructor : RoomDatabaseConstructor<MusicAppDatabase> {
    override fun initialize(): MusicAppDatabase
}
