package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.models.FavoriteSongRequest
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.FavoritesCache
import com.witelokk.musicapp.isConnectionError
import com.witelokk.musicapp.loge
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow

class FavoritesRepositoryImpl(
    private val api: FavoritesApi,
    private val cache: FavoritesCache,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<Song>?> {
        return cache.observeFavorites()
    }

    override suspend fun refreshFavorites() {
        val response = try {
            api.getFavorites()
        } catch (e: Exception) {
            if (e.isConnectionError())
                throw ConnectionErrorException(e)
            loge("FAVORITES_REPO", e.stackTraceToString())
            return
        }

        if (!response.success)
            throw ApiErrorException(response.status, response.response.bodyAsText())

        cache.cache(response.body().songs)
    }

    override suspend fun addFavorite(song: Song) {
        val response = try {
            api.addFavorite(FavoriteSongRequest(song.id))
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("FAVORITES_REPO", e.stackTraceToString())
            return
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        cache.addFavorite(song)
    }

    override suspend fun removeFavorite(songId: String) {
        val response = try {
            api.removeFavorite(songId)
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("FAVORITES_REPO", e.stackTraceToString())
            return
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        cache.removeFavorite(songId)
    }
}
