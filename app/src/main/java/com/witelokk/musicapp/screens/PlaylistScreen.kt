package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.R
import com.witelokk.musicapp.api.models.ArtistSummary
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.SongListItem
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(navController: NavController, musicPlayer: MusicPlayer) {
//    val songs = List(25) {
//        Song(
//            "https://avatars.yandex.net/get-music-content/14662984/ae9761c3.a.34843940-1/520x520",
//            "Die in My Heart",
//            listOf(
//                ArtistSummary(
//                    "123","Solid Reasons", "https://avatars.yandex.net/get-music-content/14082060/d43d35e5.p.23107413/m1000x1000"
//                )
//            ),
//            Duration.parse("2m"),
//            true
//        )
//    }
    val songs = listOf<Song>()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    PlayerSheetScaffold(
        navController,
        musicPlayer,
        topBar = {
            TopAppBar(title = { Text("Playlist") }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                }
            }, actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.PlayArrow, stringResource(R.string.play))
                }
            }, scrollBehavior = scrollBehavior)
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(songs) { track ->
                SongListItem(track,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}