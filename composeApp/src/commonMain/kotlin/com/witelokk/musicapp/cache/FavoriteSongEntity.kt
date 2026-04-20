package com.witelokk.musicapp.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val id: String,
    val sortOrder: Int,
    val songJson: String,
)
