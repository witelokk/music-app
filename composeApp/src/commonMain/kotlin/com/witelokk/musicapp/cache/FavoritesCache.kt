package com.witelokk.musicapp.cache

import com.witelokk.musicapp.api.models.Song
import kotlinx.coroutines.flow.Flow

interface FavoritesCache {
    fun observeFavorites(): Flow<List<Song>?>
    suspend fun getFavorites(): List<Song>
    suspend fun cache(songs: List<Song>)
    suspend fun addFavorite(song: Song)
    suspend fun removeFavorite(songId: String)
}
