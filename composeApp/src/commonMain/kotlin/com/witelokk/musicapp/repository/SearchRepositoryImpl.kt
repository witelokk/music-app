package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.api.models.SearchResponse
import com.witelokk.musicapp.api.models.SearchResultItem
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.FavoritesCache
import com.witelokk.musicapp.cache.PlaylistCache
import com.witelokk.musicapp.isConnectionError
import com.witelokk.musicapp.loge
import io.ktor.client.statement.bodyAsText

class SearchRepositoryImpl(
    private val api: SearchApi,
    private val favoritesCache: FavoritesCache,
    private val playlistCache: PlaylistCache,
) : SearchRepository {

    override suspend fun search(query: String, page: Int, limit: Int): SearchResponse {
        val localResults = searchLocalSongs(query)

        val apiResponse = try {
            api.search(query, type = null, page = page, limit = limit)
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                return localResults.toSearchResponse(query, page, limit)
            }
            loge("SEARCH_REPO", e.stackTraceToString())
            return localResults.toSearchResponse(query, page, limit)
        }

        if (!apiResponse.success) {
            throw ApiErrorException(apiResponse.status, apiResponse.response.bodyAsText())
        }

        val mergedResults = mergeLocalAndRemote(localResults, apiResponse.body().results)
        return apiResponse.body().copy(
            total = mergedResults.size,
            results = mergedResults,
        )
    }

    private suspend fun searchLocalSongs(query: String): List<SearchResultItem> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            return emptyList()
        }

        return buildList {
            addAll(favoritesCache.getFavorites())
            playlistCache.getPlaylists().forEach { playlist ->
                addAll(playlist.songs)
            }
        }
            .distinctBy(Song::id)
            .filter { song -> song.matches(normalizedQuery) }
            .map { song ->
                SearchResultItem(
                    type = SearchResultItem.Type.song,
                    song = song,
                )
            }
    }

    private fun Song.matches(normalizedQuery: String): Boolean {
        return name.contains(normalizedQuery, ignoreCase = true) ||
                artists.any { artist -> artist.name.contains(normalizedQuery, ignoreCase = true) }
    }

    private fun mergeLocalAndRemote(
        localResults: List<SearchResultItem>,
        remoteResults: List<SearchResultItem>,
    ): List<SearchResultItem> {
        val localSongIds = localResults
            .mapNotNull { result -> result.song?.id }
            .toSet()

        return localResults + remoteResults.filterNot { result ->
            result.type == SearchResultItem.Type.song && result.song?.id in localSongIds
        }
    }

    private fun List<SearchResultItem>.toSearchResponse(
        query: String,
        page: Int,
        limit: Int,
    ): SearchResponse {
        return SearchResponse(
            query = query,
            page = page,
            limit = limit,
            total = size,
            results = this,
        )
    }
}
