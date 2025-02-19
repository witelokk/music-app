package com.witelokk.musicapp.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(navController: NavController, modifier: Modifier, content: @Composable () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

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
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingIcon = {
                if (expanded) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurface
                        )
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
