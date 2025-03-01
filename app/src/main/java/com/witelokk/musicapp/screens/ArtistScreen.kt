package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.witelokk.musicapp.components.EntityCard
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.TrackListItem
import com.witelokk.musicapp.data.Artist
import com.witelokk.musicapp.data.Entity
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.data.Song
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(navController: NavController, playerState: PlayerState) {
    val popular = List(5) {
        Song(
            "https://avatars.yandex.net/get-music-content/14662984/ae9761c3.a.34843940-1/520x520",
            "Die in My Heart",
            listOf(
                Artist(
                    "Solid Reasons",
                    123,
                    ""
                )
            ),
            Duration.parse("2m"),
            true
        )
    }

    val releases = listOf(
        Entity(
            "Die in My Heart",
            "Single",
            "https://avatars.yandex.net/get-music-content/14662984/ae9761c3.a.34843940-1/520x520"
        ), Entity(
            "Zloy",
            "Single",
            "https://avatars.yandex.net/get-music-content/14715139/c1e5abf6.a.34140674-1/400x400"
        ), Entity(
            "Teach Me How to Lie",
            "Single",
            "https://avatars.yandex.net/get-music-content/13529784/9688d738.a.33520932-1/400x400"
        )
    )

    val artist = Artist(
        "Solid Reasons",
        123,
        "https://image-cdn-fa.spotifycdn.com/image/ab676186000001943e825ee8cbd8453f9ee05aec"
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var playlistsFilter by rememberSaveable { mutableStateOf(false) }
    var singlesEPFilter by rememberSaveable { mutableStateOf(false) }

    PlayerSheetScaffold(navController, playerState, topBar = {
        TopAppBar({
            Column {
                Text(artist.name)
            }
        }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack, "Back"
                )
            }
        }, actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.PlayArrow, "Play")
            }
        }, scrollBehavior = scrollBehavior)
    }) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    AsyncImage(
                        artist.cover, null, modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(16.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Popular",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }
            }

            items(popular, span = { GridItemSpan(2) }) { track ->
                TrackListItem(
                    track, modifier = Modifier
                        .clickable { }
                        .padding(vertical = 8.dp)
                )
            }

            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        "Releases",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(playlistsFilter,
                            onClick = { playlistsFilter = !playlistsFilter },
                            { Text("Albums") })
                        FilterChip(singlesEPFilter,
                            onClick = { singlesEPFilter = !singlesEPFilter },
                            { Text("Singles and EPs") })
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(releases) { release ->
                EntityCard(release, modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable {
                        navController.navigate("playlist")
                    })
            }
        }
    }
}
