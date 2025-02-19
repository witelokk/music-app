package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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