package com.witelokk.musicapp.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_feed")
data class HomeFeedEntity(
    @PrimaryKey val id: Int = 0,
    val layoutJson: String,
)
