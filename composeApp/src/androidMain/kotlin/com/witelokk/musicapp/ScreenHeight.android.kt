package com.witelokk.musicapp

import android.content.res.Resources
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual fun getScreenHeight(): Dp {
    val configuration = Resources.getSystem().configuration
    return configuration.screenHeightDp.toFloat().dp
}