package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.models.Song
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Song>?>
    suspend fun refreshFavorites()
    suspend fun addFavorite(song: Song)
    suspend fun removeFavorite(songId: String)
}
