package com.witelokk.musicapp.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite_songs ORDER BY sortOrder ASC")
    fun observeFavorites(): Flow<List<FavoriteSongEntity>>

    @Query("SELECT * FROM favorite_songs ORDER BY sortOrder ASC")
    suspend fun getFavorites(): List<FavoriteSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(songs: List<FavoriteSongEntity>)

    @Query("DELETE FROM favorite_songs")
    suspend fun clearFavorites()

    @Transaction
    suspend fun replaceFavorites(songs: List<FavoriteSongEntity>) {
        clearFavorites()
        insertFavorites(songs)
    }
}
