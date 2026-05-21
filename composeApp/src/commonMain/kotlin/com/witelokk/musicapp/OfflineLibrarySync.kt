package com.witelokk.musicapp

import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.cache.MediaCacheState
import com.witelokk.musicapp.repository.FavoritesRepository
import com.witelokk.musicapp.repository.PlaylistsRepository
import kotlinx.coroutines.flow.first

class OfflineLibrarySync(
    private val favoritesRepository: FavoritesRepository,
    private val playlistsRepository: PlaylistsRepository,
    private val mediaCache: MediaCache,
) {
    suspend fun sync() {
        val songsToCache = buildList {
            addAll(loadFavoriteSongs())
            addAll(loadPlaylistSongs())
        }

        songsToCache
            .asSequence()
            .map(Song::streamUrl)
            .distinct()
            .forEach { url ->
                val cacheState = mediaCache.getCacheState(url).value
                if (cacheState == MediaCacheState.NOT_CACHED || cacheState == MediaCacheState.FAILED) {
                    mediaCache.cache(url)
                }
            }
    }

    private suspend fun loadFavoriteSongs(): List<Song> {
        return try {
            favoritesRepository.refreshFavorites()
            favoritesRepository.observeFavorites().first().orEmpty()
        } catch (e: Exception) {
            loge("APP_LAUNCH_CACHE", "Failed to preload favorites: ${e.stackTraceToString()}")
            emptyList()
        }
    }

    private suspend fun loadPlaylistSongs(): List<Song> {
        val playlistSummaries = try {
            playlistsRepository.getPlaylists()
        } catch (e: Exception) {
            loge("APP_LAUNCH_CACHE", "Failed to preload playlists: ${e.stackTraceToString()}")
            return emptyList()
        }

        return buildList {
            playlistSummaries.forEach { playlist ->
                try {
                    playlistsRepository.refreshPlaylist(playlist.id)
                    addAll(
                        playlistsRepository.observePlaylist(playlist.id).first()?.songs.orEmpty()
                    )
                } catch (e: Exception) {
                    loge(
                        "APP_LAUNCH_CACHE",
                        "Failed to preload playlist ${playlist.id}: ${e.stackTraceToString()}"
                    )
                }
            }
        }
    }
}
