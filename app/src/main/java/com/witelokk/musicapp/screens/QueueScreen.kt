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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.TrackListItem
import com.witelokk.musicapp.data.Artist
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.data.Song
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(navController: NavController, playerState: PlayerState) {
    val songs = List(25) {
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

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            confirmValueChange = { true },
            skipHiddenState = true,
        )
    )

    LaunchedEffect(Unit) {
        scaffoldState.bottomSheetState.partialExpand()
    }

    PlayerSheetScaffold(
        navController,
        playerState,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = { Text("Queue") }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                }
            }, scrollBehavior = scrollBehavior)
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(songs) { track ->
                TrackListItem(track,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}