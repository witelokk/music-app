package com.witelokk.musicapp.cache

import com.witelokk.musicapp.api.models.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PlaylistCacheImpl(
    database: MusicAppDatabase,
    private val json: Json,
) : PlaylistCache {
    private val playlistDao = database.playlistDao()

    override fun observePlaylist(id: String): Flow<Playlist?> {
        return playlistDao.observePlaylist(id).map { entity ->
            entity?.let { json.decodeFromString<Playlist>(it.playlistJson) }
        }
    }

    override suspend fun getPlaylists(): List<Playlist> {
        return playlistDao.getPlaylists().map { entity ->
            json.decodeFromString<Playlist>(entity.playlistJson)
        }
    }

    override suspend fun cache(playlist: Playlist) {
        playlistDao.insertPlaylist(
            PlaylistEntity(
                id = playlist.id,
                playlistJson = json.encodeToString(playlist),
            )
        )
    }

    override suspend fun deletePlaylist(id: String) {
        playlistDao.deletePlaylist(id)
    }

    override suspend fun updatePlaylistName(id: String, name: String) {
        updatePlaylist(id) { it.copy(name = name) }
    }

    override suspend fun removeSongFromPlaylist(id: String, songId: String) {
        updatePlaylist(id) { playlist ->
            playlist.copy(songs = playlist.songs.filterNot { it.id == songId })
        }
    }

    override suspend fun updateSongFavorite(id: String, songId: String, favorite: Boolean) {
        updatePlaylist(id) { playlist ->
            playlist.copy(
                songs = playlist.songs.map { song ->
                    if (song.id == songId) song.copy(isFavorite = favorite) else song
                }
            )
        }
    }

    override suspend fun prependSongToPlaylist(id: String, song: Song) {
        updatePlaylist(id) { playlist ->
            playlist.copy(
                songs = listOf(song) + playlist.songs.filterNot { it.id == song.id }
            )
        }
    }

    private suspend fun updatePlaylist(id: String, update: (Playlist) -> Playlist) {
        val entity = playlistDao.getPlaylist(id) ?: return
        val updated = update(json.decodeFromString(entity.playlistJson))
        cache(updated)
    }
}
