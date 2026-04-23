package com.witelokk.musicapp.cache

import com.witelokk.musicapp.api.models.Song
import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val coverUrl: String? = null,
    val songs: List<Song>,
)
