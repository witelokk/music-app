package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    fun observePlaylist(id: String): Flow<Playlist?>
    suspend fun refreshPlaylist(id: String)
    suspend fun getPlaylists(): List<PlaylistSummary>
    suspend fun deletePlaylist(id: String)
    suspend fun renamePlaylist(id: String, name: String)
    suspend fun removeSongFromPlaylist(id: String, songId: String)
    suspend fun updateCachedSongFavorite(id: String, songId: String, favorite: Boolean)
    suspend fun prependSongToPlaylist(id: String, song: Song)
}
