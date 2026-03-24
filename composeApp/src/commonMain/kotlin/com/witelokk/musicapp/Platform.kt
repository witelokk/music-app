package com.witelokk.musicapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform