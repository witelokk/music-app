package com.witelokk.musicapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCacheState
import com.witelokk.musicapp.data.PlayerState
import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.flow.StateFlow
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.add_to_playlist
import musicapp.composeapp.generated.resources.add_to_queue
import org.jetbrains.compose.resources.stringResource

@Composable
fun SongCollectionList(
    songs: List<Song>,
    playerState: PlayerState?,
    connectivityStatus: Connectivity.Status?,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showFavorite: Boolean = true,
    showAddToQueueMenuItem: Boolean = true,
    songCacheState: (Song) -> StateFlow<MediaCacheState>,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit = {},
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    extraMenuItems: @Composable (song: Song, menuExpanded: MutableState<Boolean>) -> Unit = { _, _ -> },
) {
    if (songs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage)
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(songs, key = { it.id }) { song ->
            val cacheState by songCacheState(song).collectAsStateWithLifecycle()
            val isAvailable = connectivityStatus is Connectivity.Status.Connected ||
                cacheState == MediaCacheState.CACHED

            SongListItem(
                song = song,
                isActive = playerState?.currentSong?.id == song.id,
                isPlaying = playerState?.playing ?: false,
                showFavorite = showFavorite,
                isAvailable = isAvailable,
                cacheState = cacheState,
                onFavoriteClick = { onFavoriteClick(song) },
                modifier = Modifier
                    .clickable(enabled = isAvailable) { onSongClick(song) }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .animateItem()
            ) { menuExpanded ->
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.add_to_playlist)) },
                    onClick = {
                        menuExpanded.value = false
                        onAddToPlaylist(song)
                    }
                )
                if (showAddToQueueMenuItem) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.add_to_queue)) },
                        onClick = {
                            onAddToQueue(song)
                            menuExpanded.value = false
                        }
                    )
                }
                extraMenuItems(song, menuExpanded)
            }
        }
    }
}
