package com.witelokk.musicapp

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
actual fun getScreenHeight(): Dp {
    val bounds = UIScreen.mainScreen.bounds
    return CGRectGetHeight(bounds).toFloat().dp
}