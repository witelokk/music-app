package com.witelokk.musicapp

import android.util.Log

actual fun logd(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun loge(tag: String, message: String) {
    Log.e(tag, message)
}
