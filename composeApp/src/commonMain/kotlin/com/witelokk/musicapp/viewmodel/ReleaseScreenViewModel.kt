package com.witelokk.musicapp.viewmodel

import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.ReleasesApi
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCache
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.repository.ConnectionErrorException
import com.witelokk.musicapp.repository.PlaylistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReleaseScreenState(
    override val isLoading: Boolean = true,
    val hasObservedFeed: Boolean = false,
    val hasCachedFeed: Boolean = false,
    override val isConnectionError: Boolean = false,
    override val isError: Boolean = false,
    override val songs: List<Song> = listOf(),
    val name: String = "",
    val coverUrl: String? = null,
    override val playlists: List<PlaylistSummary> = listOf(),
    override val playerState: PlayerState?,
) : SongListScreenState

class ReleaseScreenViewModel(
    private val playlistsApi: PlaylistsApi,
    private val releasesApi: ReleasesApi,
    playlistsRepository: PlaylistsRepository,
    favoritesApi: FavoritesApi,
    musicPlayer: MusicPlayer,
    mediaCache: MediaCache,
) : SongListViewModel<ReleaseScreenState>(
    musicPlayer,
    favoritesApi,
    playlistsApi,
    mediaCache,
    playlistsRepository
) {
    private val _state = MutableStateFlow(ReleaseScreenState(playerState = musicPlayer.state.value))
    override val state: StateFlow<ReleaseScreenState> = _state.asStateFlow()

    override fun MutableStateFlowAccessor(): MutableStateFlow<ReleaseScreenState> = _state

    override fun copyWithPlayerState(
        state: ReleaseScreenState,
        playerState: PlayerState?
    ) = state.copy(playerState = playerState)

    override fun copyWithPlaylists(
        state: ReleaseScreenState,
        playlists: List<PlaylistSummary>
    ) = state.copy(playlists = playlists)

    override fun copyWithSongs(
        state: ReleaseScreenState,
        songs: List<Song>
    ) = state.copy(songs = songs)

    init {
        bindPlayerState()
    }

    fun loadRelease(id: String) {
        updateState { ReleaseScreenState(playerState = it.playerState) }
        launchCatching(action = "load release $id", onError = {
            updateState { state ->
                state.copy(
                    isConnectionError = it is ConnectionErrorException,
                    isError = true,
                    isLoading = false,
                )
            }
        }) {
            val response = releasesApi.getRelease(id)

            if (response.logIfFailure("load release $id")) {
                updateState { it.copy(isError = true, isLoading = false) }
                return@launchCatching
            }

            val release = response.body()

            updateState {
                it.copy(
                    isLoading = false,
                    hasObservedFeed = true,
                    hasCachedFeed = false,
                    isConnectionError = false,
                    isError = false,
                    songs = release.songs.songs,
                    name = release.name,
                    coverUrl = release.coverUrl,
                )
            }
        }
    }
}
