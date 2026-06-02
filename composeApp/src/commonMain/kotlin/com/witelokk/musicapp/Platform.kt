package com.witelokk.musicapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openAppLanguageSettings(): Boolean

expect fun isActiveNetworkMetered(): Boolean

expect fun supportsDynamicColors(): Boolean
