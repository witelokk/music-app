package com.witelokk.musicapp.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeFeedDao {
    @Query("SELECT * FROM home_feed WHERE id = 0")
    fun observeFeed(): Flow<HomeFeedEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(layout: HomeFeedEntity)
}
