package com.witelokk.musicapp.viewmodel

import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState

interface SongListScreenState {
    val isLoading: Boolean
    val isConnectionError: Boolean
    val isError: Boolean
    val songs: List<Song>
    val playlists: List<PlaylistSummary>
    val playerState: PlayerState?
}
