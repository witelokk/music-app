package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.artist_placeholder
import org.jetbrains.compose.resources.painterResource

@Composable
fun Card(
    title: String,
    pictureUrl: String?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    fixedSize: Boolean = false,
) {
    var isImageLoading by remember { mutableStateOf(true) }
    OutlinedCard(modifier = if (fixedSize) modifier.requiredWidth(165.dp) else modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = if (fixedSize) Modifier.requiredSize(165.dp) else Modifier.fillMaxWidth().aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    pictureUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { isImageLoading = false },
                    onError = { isImageLoading = false },
                    contentDescription = null,
                    error = painterResource(Res.drawable.artist_placeholder),
                )
                if (isImageLoading) {
                    CircularProgressIndicator()
                }
            }
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
