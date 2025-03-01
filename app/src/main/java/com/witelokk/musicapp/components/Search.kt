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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(navController: NavController, modifier: Modifier, expanded: MutableState<Boolean> = rememberSaveable {mutableStateOf(false)}, content: @Composable () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by expanded

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
            onQueryChange = { query = it },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            placeholder = { Text("Search songs, albums and artists") },
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
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    IconButton(onClick = {}) {
                        Avatar("R",
                            modifier = Modifier.clickable { navController.navigate("settings") })
                    }
                }
            },
            onSearch = {},
        )
    }, expanded = expanded, onExpandedChange = { expanded = it }, content = { content() })
}

@Composable
fun SearchContent(modifier: Modifier = Modifier) {
    val filters = listOf("Saved", "Playlists", "Track", "Artists")
    val selected = List(filters.size) { rememberSaveable { mutableStateOf(false) } }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEachIndexed { i, filter ->
                FilterChip(
                    selected[i].value,
                    { selected[i].value = !selected[i].value },
                    { Text(filter) },
                )
            }
        }
    }
}

@Composable
fun SearchEmptyContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No results", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SearchFailedContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Search failed", modifier = Modifier.padding(16.dp))
        Button(onClick = {}) {
            Icon(Icons.Default.Replay, "Retry")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}