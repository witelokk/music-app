package com.witelokk.musicapp.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.screens.ArtistScreenRoute
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${if (seconds < 10) "0$seconds" else seconds}"
}

fun calculateSliderPosition(playerState: PlayerState?): Float {
    if (playerState == null) {
        return 0f
    }

    return (1f * (playerState.currentPosition.inWholeSeconds) / (playerState.currentSong.durationSeconds))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Player(
    navController: NavController,
    sheetState: SheetState,
    playerState: PlayerState,
    onSeek: (Duration) -> Unit,
    onSeekToPrevious: () -> Unit,
    onSeekToNext: () -> Unit,
    onPlayPause: () -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onChangeFavorite: (Song, Boolean) -> Unit,
    onPlaySongInQueue: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember {
        mutableFloatStateOf(
            calculateSliderPosition(playerState)
        )
    }
    var openArtistsDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { playerState.queue.size })
    var previousPage by remember { mutableStateOf(0) }

    LaunchedEffect(playerState.currentPosition) {
        sliderPosition = calculateSliderPosition(playerState)
    }

    LaunchedEffect(playerState.currentSong) {
        pagerState.animateScrollToPage(playerState.currentSongIndex)
    }

    LaunchedEffect(pagerState.targetPage) {
        if (playerState.currentSongIndex != pagerState.targetPage) {
            onPlaySongInQueue(pagerState.targetPage)
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(pagerState, pageSpacing = 16.dp) { i ->
            AsyncImage(
                playerState.queue[i].coverUrl ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6)),
                contentScale = ContentScale.FillWidth,
                contentDescription = null,
                error = painterResource(R.drawable.artist_placeholder)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playerState.currentSong.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.basicMarquee()
                )
                Text(playerState.currentSong.artists.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable {
                            if (playerState.currentSong.artists.size == 1) {
                                scope.launch {
                                    sheetState.partialExpand()
                                    navController.navigate(ArtistScreenRoute(playerState.currentSong.artists[0].id))
                                }
                            } else {
                                openArtistsDialog = true
                            }
                        }
                        .basicMarquee())
            }
            IconButton(onClick = {
                scope.launch {
                    sheetState.partialExpand()
                    if (navController.currentDestination?.route !== "queue") {
                        navController.navigate("queue")
                    }
                }
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = { onAddToPlaylist(playerState.currentSong) }) {
                Icon(
                    Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = {
                onChangeFavorite(
                    playerState.currentSong,
                    !playerState.currentSong.isFavorite
                )
            }) {
                Icon(
                    if (playerState.currentSong.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (playerState.currentSong.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDuration(playerState.currentPosition))
            Text(formatDuration(playerState.currentSong.durationSeconds.seconds))
        }

        Slider(modifier = Modifier.padding(horizontal = 16.dp),
            value = sliderPosition,
            onValueChange = {
                onSeek((playerState.currentSong.durationSeconds * it).toInt().seconds)
                sliderPosition = it
            })

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onSeekToPrevious() }) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            Button(modifier = Modifier
                .width(100.dp)
                .height(100.dp), onClick = { onPlayPause() }) {
                Icon(
                    if (playerState.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            IconButton(onClick = { onSeekToNext() }) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text("")
        Text("")
        Text("")
    }

    when {
        openArtistsDialog -> AlertDialog(onDismissRequest = {
            openArtistsDialog = false
        }, title = { Text("Artists") }, confirmButton = {
            TextButton(onClick = {
                openArtistsDialog = false
            }) { Text("Close") }
        }, text = {
            LazyColumn {
                items(playerState.currentSong.artists) { artist ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                scope.launch {
                                    sheetState.partialExpand()
                                    navController.navigate(ArtistScreenRoute(playerState.currentSong.artists[0].id))
                                }
                            }
                            .fillParentMaxWidth()
                            .padding(vertical = 8.dp)) {
                        AsyncImage(
                            artist.avatarUrl,
                            "",
                            error = painterResource(R.drawable.artist_placeholder),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(
                                    RoundedCornerShape(100)
                                )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(artist.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        })
    }
}
