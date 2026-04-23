package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.api.models.UpdatePlaylistRequest
import com.witelokk.musicapp.cache.PlaylistCache
import com.witelokk.musicapp.cache.Playlist
import com.witelokk.musicapp.isConnectionError
import com.witelokk.musicapp.loge
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow

class PlaylistsRepositoryImpl(
    private val playlistsApi: PlaylistsApi,
    private val cache: PlaylistCache,
) : PlaylistsRepository {

    override fun observePlaylist(id: String): Flow<Playlist?> {
        return cache.observePlaylist(id)
    }

    override suspend fun refreshPlaylist(id: String) {
        val response = try {
            playlistsApi.getPlaylist(id, includeSongs = true)
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("PLAYLIST_REPO", e.stackTraceToString())
            return
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        val playlist = response.body()
        cache.cache(
            Playlist(
                id = playlist.id,
                name = playlist.name,
                coverUrl = playlist.coverUrl,
                songs = playlist.songs?.songs.orEmpty(),
            )
        )
    }

    override suspend fun getPlaylists(): List<PlaylistSummary> {
        val response = try {
            playlistsApi.getPlaylists()
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("PLAYLIST_REPO", e.stackTraceToString())
            return emptyList()
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        return response.body().playlists
    }

    override suspend fun deletePlaylist(id: String) {
        val response = try {
            playlistsApi.deletePlaylist(id)
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("PLAYLIST_REPO", e.stackTraceToString())
            return
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        cache.deletePlaylist(id)
    }

    override suspend fun renamePlaylist(id: String, name: String) {
        val response = try {
            playlistsApi.updatePlaylist(id, UpdatePlaylistRequest(name))
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("PLAYLIST_REPO", e.stackTraceToString())
            return
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        cache.updatePlaylistName(id, name)
    }

    override suspend fun removeSongFromPlaylist(id: String, songId: String) {
        val response = try {
            playlistsApi.removeSongFromPlaylist(id, songId)
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("PLAYLIST_REPO", e.stackTraceToString())
            return
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        cache.removeSongFromPlaylist(id, songId)
    }

    override suspend fun updateCachedSongFavorite(id: String, songId: String, favorite: Boolean) {
        cache.updateSongFavorite(id, songId, favorite)
    }

    override suspend fun prependSongToPlaylist(id: String, song: Song) {
        cache.prependSongToPlaylist(id, song)
    }
}
