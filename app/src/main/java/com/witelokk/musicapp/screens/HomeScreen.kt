package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.components.AddCard
import com.witelokk.musicapp.components.FavoriteCard
import com.witelokk.musicapp.components.EntityCard
import com.witelokk.musicapp.components.PlayerSheetScaffold
import com.witelokk.musicapp.components.Search
import com.witelokk.musicapp.components.SearchContent
import com.witelokk.musicapp.data.Entity
import com.witelokk.musicapp.data.HomeLayout
import com.witelokk.musicapp.data.PlayerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, playerState: PlayerState) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    val layout = HomeLayout(
        playlists = listOf(), sections = listOf(
            Pair(
                "Recent", listOf(
                    Entity(
                        "Solid Reasons",
                        "Artist",
                        "https://avatars.yandex.net/get-music-content/14728505/65f75b6e.p.23107413/400x400"
                    ),
                    Entity(
                        "4locked",
                        "Artist",
                        "https://avatars.yandex.net/get-music-content/12799091/a464f783.a.34980238-1/520x520"
                    ),
                    Entity(
                        "Solid Reasons",
                        "Artist",
                        "https://avatars.yandex.net/get-music-content/14728505/65f75b6e.p.23107413/400x400"
                    ),
                )
            ),
            Pair(
                "Some songs", listOf(
                    Entity(
                        "Die in My Heart",
                        "Solid Reasons",
                        "https://avatars.yandex.net/get-music-content/14662984/ae9761c3.a.34843940-1/520x520"
                    ), Entity(
                        "Zloy",
                        "Solid Reasons",
                        "https://avatars.yandex.net/get-music-content/14715139/c1e5abf6.a.34140674-1/400x400"
                    ), Entity(
                        "Не могу найти",
                        "4locked",
                        "https://avatars.yandex.net/get-music-content/14369544/e0658469.a.34278387-2/400x400"
                    )
                )
            ),
        )
    )

    PlayerSheetScaffold(navController, playerState) { innerPadding ->
        Column {
            Search(navController, modifier = Modifier.fillMaxWidth()) { SearchContent() }
            LazyColumn(contentPadding = innerPadding) {
                item {
                    Text(
                        "Playlists",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(24.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            FavoriteCard(modifier = Modifier.clickable { navController.navigate("playlist") })
                        }
                        items(layout.playlists) { item ->
                            Spacer(modifier = Modifier.width(16.dp))
                            EntityCard(item,
                                modifier = Modifier.clickable { navController.navigate("playlist") })
                        }
                        item {
                            AddCard(modifier = Modifier.clickable { navController.navigate("playlist") })
                        }
                    }
                }
                items(layout.sections) { (title, entities) ->
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(24.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(entities) { entity ->
                            EntityCard(entity,
                                modifier = Modifier.width(155.dp)
                                    .clickable { navController.navigate("artist") })
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

