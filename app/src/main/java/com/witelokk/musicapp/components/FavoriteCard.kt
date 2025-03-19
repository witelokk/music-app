package com.witelokk.musicapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.witelokk.musicapp.R

@Composable
fun FavoriteCard(modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier.width(155.dp)) {
        Column {
            Box(
                modifier = Modifier
                    .size(155.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Favorite,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(96.dp)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.favorite_songs), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}