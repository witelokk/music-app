package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R
import com.witelokk.musicapp.data.Entity

@Composable
fun EntityCard(entity: Entity, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier) {
        Column {
            AsyncImage(
                entity.pictureUrl,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                contentDescription = null,
                error = painterResource(R.drawable.artist_placeholder)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(entity.name, style = MaterialTheme.typography.titleMedium)
                Text(entity.type, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}