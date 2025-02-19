package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Avatar(text: String, modifier: Modifier = Modifier, radius: Float = 36f) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(modifier=modifier.padding(2.dp).drawBehind {
        drawCircle(color = primary, radius = radius)
    }) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            style = TextStyle(color = onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        )
    }
}