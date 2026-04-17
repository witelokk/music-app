package com.witelokk.musicapp

import platform.Foundation.NSLog

actual fun logd(tag: String, message: String) {
    NSLog("DEBUG: [$tag] $message")
}

actual fun loge(tag: String, message: String) {
    NSLog("ERROR: [$tag] $message")
}
