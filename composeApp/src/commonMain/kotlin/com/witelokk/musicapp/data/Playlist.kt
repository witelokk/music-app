package com.witelokk.musicapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Playlist (
    val id: String,
    val name: String,
    val songsCount: Int,
    val coverUrl: String? = null
)
