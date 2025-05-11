package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.MusicPlayer
import com.witelokk.musicapp.R
import com.witelokk.musicapp.api.models.Song
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.SongListItem
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(navController: NavController) {
    val songs = listOf<Song>()
    val playerState by koinInject<MusicPlayer>().state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    PlayerSheetScaffold(
        navController,
        playerState = playerState,
        onSeek = {},
        onSeekToPrevious = {},
        onSeekToNext = {},
        onPlayPause = {},
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.queue)) }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}