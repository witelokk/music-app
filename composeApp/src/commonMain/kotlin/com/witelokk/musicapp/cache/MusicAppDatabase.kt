package com.witelokk.musicapp.cache

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.AutoMigration

@Database(
    entities = [FavoriteSongEntity::class, HomeFeedEntity::class, PlaylistEntity::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
    exportSchema = true,
)
@ConstructedBy(MusicAppDatabaseConstructor::class)
abstract class MusicAppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun homeFeedDao(): HomeFeedDao
    abstract fun playlistDao(): PlaylistDao
}

@Suppress("KotlinNoActualForExpect")
expect object MusicAppDatabaseConstructor : RoomDatabaseConstructor<MusicAppDatabase> {
    override fun initialize(): MusicAppDatabase
}
