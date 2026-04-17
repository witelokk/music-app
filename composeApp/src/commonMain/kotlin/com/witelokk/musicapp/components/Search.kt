package com.witelokk.musicapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.api.models.SearchResultItem
import com.witelokk.musicapp.data.Playlist
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    navController: NavController,
    modifier: Modifier = Modifier,
    expanded: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    onQueryChanged: (String) -> Unit = {},
    avatar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by expanded

    val keyboardController = LocalSoftwareKeyboardController.current

    val searchBarPadding by animateDpAsState(
        targetValue = if (expanded) 0.dp else 16.dp, label = "Search bar padding"
    )

    LaunchedEffect(expanded) {
        if (!expanded) {
            query = ""
        }
    }

    SearchBar(modifier = modifier.padding(horizontal = searchBarPadding), inputField = {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = { query = it; onQueryChanged(it) },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            placeholder = {
                Text(
                    stringResource(Res.string.search_placeholder),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = {
                if (expanded) {
                    IconButton(onClick = { expanded = false }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            trailingIcon = {
                if (expanded) {
                    AnimatedVisibility(query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                        IconButton(onClick = {
                            query =
                                ""; onQueryChanged(""); keyboardController?.hide()
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    IconButton(onClick = {}) { avatar() }
                }
            },
            onSearch = {},
        )
    }, expanded = expanded, onExpandedChange = { expanded = it }, content = { content() })
}

@Composable
fun SearchResults(
    results: List<SearchResultItem>,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
    onResultClick: (SearchResultItem) -> Unit = {},
    filter: String? = null
) {
    LazyColumn(modifier = modifier) {
        items(results) {
            if (it.type == "song" && (filter == "Songs" || filter == null)) {
                SongListItem(
                    it.song!!,
                    modifier = itemModifier.clickable { onResultClick(it) },
                )
            } else if (it.type == "artist" && (filter == "Artists" || filter == null)) {
                ArtistListItem(
                    it.artist!!,
                    modifier = itemModifier.clickable { onResultClick(it) },
                )
            } else if (it.type == "playlist" && (filter == "Playlists" || filter == null)) {
                PlaylistListItem(
                    Playlist(
                        name = it.playlist!!.name,
                        coverUrl = it.playlist.coverUrl,
                        id = it.playlist.id,
                        songsCount = it.playlist.songsCount,
                    ),
                    modifier = itemModifier.clickable { onResultClick(it) },
                )
            }
        }
    }
}

@Composable
fun SearchSuccessfulContent(
    results: List<SearchResultItem>,
    modifier: Modifier = Modifier,
    onResultClick: (SearchResultItem) -> Unit = {},
) {
    val filters = listOf("Playlists", "Songs", "Artists")
    val selected = List(filters.size) { rememberSaveable { mutableStateOf(false) } }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }

    fun onFilterSelected(index: Int) {
        filters.forEachIndexed { i, _ ->
            if (i != index) {
                selected[i].value = false
            }
        }

        selected[index].value = !selected[index].value

        filter = if (selected[index].value) {
            filters[index]
        } else {
            null
        }
    }

    Column(modifier = modifier) {
        if (results.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEachIndexed { i, filter ->
                    FilterChip(
                        selected[i].value,
                        {
                            onFilterSelected(i)
                        },
                        { Text(filter) },
                    )
                }
            }
        }
        SearchResults(
            results,
            filter = filter,
            itemModifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            onResultClick = onResultClick
        )
    }
}

@Composable
fun SearchHistoryContent(
    results: List<SearchResultItem>,
    modifier: Modifier = Modifier,
    onResultClick: (SearchResultItem) -> Unit = {},
    onClearClick: () -> Unit = {},
) {
    if (results.isEmpty()) {
        return
    }

    Column(modifier = modifier) {
        Text(stringResource(Res.string.recent_searches), modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
        SearchResults(
            results.reversed(),
            itemModifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            onResultClick = onResultClick
        )
        TextButton(onClearClick) {
            Text(stringResource(Res.string.clear), modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun SearchEmptyContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(Res.string.no_results), modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SearchFailedContent(retry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(Res.string.search_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = { retry() }) {
            Icon(Icons.Default.Replay, stringResource(Res.string.retry))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.retry))
        }
    }
}

@Composable
fun SearchLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}