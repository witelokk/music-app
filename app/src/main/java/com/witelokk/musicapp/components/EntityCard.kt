package com.witelokk.musicapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R
import com.witelokk.musicapp.data.Entity

@Composable
fun EntityCard(entity: Entity, modifier: Modifier = Modifier) {
    var isImageLoading by remember { mutableStateOf(true) }
    OutlinedCard(modifier = modifier) {
        Column {
            if (isImageLoading) {
                Box(
                    modifier = Modifier.size(155.dp),
//                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AsyncImage(
                    entity.pictureUrl,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                    onSuccess = { isImageLoading = false },
                    onError = { isImageLoading = false },
                    contentDescription = null,
                    error = painterResource(R.drawable.artist_placeholder)
                    // TODO: FIX NOT SHOWING IMAGE AND ADD THE SAME STUFF TO PLAYER
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(entity.name, style = MaterialTheme.typography.titleMedium)
                Text(entity.type, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}