package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R

@Composable
fun Card(
    title: String,
    pictureUrl: String?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    var isImageLoading by remember { mutableStateOf(true) }
    OutlinedCard(modifier = modifier.width(165.dp)) {
        Column {
            if (isImageLoading) {
                Box(
                    modifier = Modifier.size(155.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            AsyncImage(
                pictureUrl,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                onSuccess = { isImageLoading = false },
                onError = { isImageLoading = false },
                contentDescription = null,
                error = painterResource(R.drawable.artist_placeholder),
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}