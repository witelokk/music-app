package com.witelokk.musicapp.cache

import com.witelokk.musicapp.api.models.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistCache {
    fun observePlaylist(id: String): Flow<Playlist?>
    suspend fun cache(playlist: Playlist)
    suspend fun deletePlaylist(id: String)
    suspend fun updatePlaylistName(id: String, name: String)
    suspend fun removeSongFromPlaylist(id: String, songId: String)
    suspend fun updateSongFavorite(id: String, songId: String, favorite: Boolean)
    suspend fun prependSongToPlaylist(id: String, song: Song)
}
