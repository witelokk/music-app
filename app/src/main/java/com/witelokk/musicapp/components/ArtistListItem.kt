package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.witelokk.musicapp.R
import com.witelokk.musicapp.data.Artist

@Composable
fun ArtistListItem(
    artist: Artist, modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            artist.cover,
            "Cover",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp)),
            error = painterResource(R.drawable.artist_placeholder)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                artist.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.height(24.dp)
            )
            Text("Artist", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
