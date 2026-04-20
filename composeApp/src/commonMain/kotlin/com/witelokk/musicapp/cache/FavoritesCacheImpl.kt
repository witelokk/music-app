package com.witelokk.musicapp.cache

import com.witelokk.musicapp.api.models.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class FavoritesCacheImpl(
    private val database: MusicAppDatabase,
    private val json: Json,
) : FavoritesCache {
    private val favoritesDao = database.favoritesDao()

    override fun observeFavorites(): Flow<List<Song>?> {
        return favoritesDao.observeFavorites().map { favorites ->
            favorites.map { favorite ->
                json.decodeFromString<Song>(favorite.songJson)
            }
        }
    }

    override suspend fun cache(songs: List<Song>) {
        favoritesDao.replaceFavorites(songs.toEntities())
    }

    override suspend fun addFavorite(song: Song) {
        val currentFavorites = favoritesDao.getFavorites()
            .map { favorite -> json.decodeFromString<Song>(favorite.songJson) }
            .filterNot { favorite -> favorite.id == song.id }

        favoritesDao.replaceFavorites((listOf(song.copy(isFavorite = true)) + currentFavorites).toEntities())
    }

    override suspend fun removeFavorite(songId: String) {
        val currentFavorites = favoritesDao.getFavorites()
            .map { favorite -> json.decodeFromString<Song>(favorite.songJson) }
            .filterNot { favorite -> favorite.id == songId }

        favoritesDao.replaceFavorites(currentFavorites.toEntities())
    }

    private fun List<Song>.toEntities(): List<FavoriteSongEntity> {
        return mapIndexed { index, song ->
            FavoriteSongEntity(
                id = song.id,
                sortOrder = index,
                songJson = json.encodeToString(song),
            )
        }
    }
}
