package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddCard(modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier) {
        Column {
            Box(
                modifier = Modifier
                    .width(155.dp)
                    .height(212.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Add,
                    "Create playlist",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .3f),
                    modifier = Modifier.size(96.dp)
                )
            }
        }
    }
}