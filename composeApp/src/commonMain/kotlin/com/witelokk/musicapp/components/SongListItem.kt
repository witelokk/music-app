package com.witelokk.musicapp.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.cache.MediaCacheState
import kotlinx.coroutines.delay
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.artist_placeholder
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun SongListItem(
    song: Song,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true,
    showDuration: Boolean = false,
    isAvailable: Boolean = true,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    cacheState: MediaCacheState = MediaCacheState.NOT_CACHED,
    onFavoriteClick: () -> Unit = {},
    dropdownMenuItems: @Composable (MutableState<Boolean>) -> Unit = {},
) {
    val menuExpandedState = remember { mutableStateOf(false) }
    var menuExpanded by menuExpandedState

    Row(
        modifier = modifier.alpha(if (!isAvailable) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongArtwork(song = song, isActive = isActive, isPlaying = isPlaying)

        Spacer(modifier = Modifier.width(16.dp))

        SongMetadata(
            song = song,
            isActive = isActive,
            showDuration = showDuration,
            cacheState = cacheState,
            modifier = Modifier.weight(1f)
        )

        SongActions(
            isAvailable = isAvailable,
            showFavorite = showFavorite,
            isFavorite = song.isFavorite,
            menuExpanded = menuExpanded,
            menuExpandedState = menuExpandedState,
            onFavoriteClick = onFavoriteClick,
            dropdownMenuItems = dropdownMenuItems,
            onMenuClick = { menuExpanded = true },
            onMenuDismiss = { menuExpanded = false }
        )
    }
}


@Composable
fun EqualizerBars(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    barCount: Int = 5,
    color: Color = Color(0xFF00FFAA),
    spacingRatio: Float = 0.2f,
    animationDuration: Int = 75,
    animationDelay: Long = 100L,
    cornerRadiusDp: Dp = 6.dp,
    playing: Boolean = true
) {
    val density = LocalDensity.current

    val widthPx = with(density) { width.toPx() }
    val heightPx = with(density) { height.toPx() }
    val cornerRadiusPx = with(density) { cornerRadiusDp.toPx() }

    val spacingPx = widthPx / (barCount / spacingRatio + barCount - 1)
    val barWidthPx = (widthPx - spacingPx * (barCount - 1)) / barCount

    val heights = remember {
        List(barCount) { Animatable(0f) }
    }

    LaunchedEffect(barCount, playing) {
        while (playing) {
            heights.forEach { anim ->
                val target = Random.nextFloat() * heightPx
                anim.animateTo(
                    target,
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
            delay(animationDelay)
        }
    }

    Row(
        modifier = modifier.size(width, height),
        horizontalArrangement = Arrangement.spacedBy(with(density) { spacingPx.toDp() })
    ) {
        heights.forEach { heightAnim ->
            Canvas(
                modifier = Modifier
                    .width(with(density) { barWidthPx.toDp() })
                    .fillMaxHeight()
            ) {
                val barHeight = heightAnim.value
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - barHeight),
                    size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        cornerRadiusPx,
                        cornerRadiusPx
                    )
                )
            }
        }
    }
}


@Composable
private fun SongArtwork(
    song: Song,
    isActive: Boolean,
    isPlaying: Boolean,
) {
    Box(contentAlignment = Alignment.Center) {
        AsyncImage(
            song.coverUrl,
            "Cover",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .alpha(if (isActive) 0.5f else 1f),
            error = painterResource(Res.drawable.artist_placeholder)
        )

        if (isActive) {
            EqualizerBars(
                width = 30.dp,
                height = 30.dp,
                color = MaterialTheme.colorScheme.primary,
                playing = isPlaying
            )
        }
    }
}

@Composable
private fun SongMetadata(
    song: Song,
    isActive: Boolean,
    showDuration: Boolean,
    cacheState: MediaCacheState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            song.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.Unspecified,
            modifier = Modifier.height(24.dp)
        )

        if (showDuration) {
            Text(
                formatDuration(song.durationSeconds.seconds),
                style = MaterialTheme.typography.bodyMedium
            )
            return
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            CacheStateIndicator(cacheState)

            Text(
                song.artists.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CacheStateIndicator(cacheState: MediaCacheState) {
    when (cacheState) {
        MediaCacheState.CACHED -> {
            Icon(
                Icons.Default.DownloadDone,
                contentDescription = "Cached",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        MediaCacheState.IN_PROGRESS -> {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        MediaCacheState.NOT_CACHED,
        MediaCacheState.FAILED -> Unit
    }
}

@Composable
private fun SongActions(
    isAvailable: Boolean,
    showFavorite: Boolean,
    isFavorite: Boolean,
    menuExpanded: Boolean,
    menuExpandedState: MutableState<Boolean>,
    onFavoriteClick: () -> Unit,
    dropdownMenuItems: @Composable (MutableState<Boolean>) -> Unit,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
) {
    LaunchedEffect(isAvailable) {
        if (!isAvailable && menuExpanded) {
            onMenuDismiss()
        }
    }

    if (showFavorite) {
        IconButton(
            onClick = onFavoriteClick,
            enabled = isAvailable,
            modifier = Modifier.offset(x = 16.dp)
        ) {
            Icon(
                if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    IconButton(
        onClick = onMenuClick,
        enabled = isAvailable,
        modifier = Modifier.offset(x = 16.dp)
    ) {
        Icon(Icons.Default.MoreVert, "More")
    }

    BottomSheetMenu(menuExpanded, onDismissRequest = onMenuDismiss) {
        dropdownMenuItems(menuExpandedState)
    }
}
