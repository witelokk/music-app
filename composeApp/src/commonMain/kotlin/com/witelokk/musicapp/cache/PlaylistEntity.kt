package com.witelokk.musicapp.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_cache")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val playlistJson: String,
)
