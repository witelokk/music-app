package com.witelokk.musicapp.cache

import kotlinx.coroutines.flow.StateFlow

interface MediaCache {
    fun cache(url: String)
    fun isCached(url: String): StateFlow<Boolean>
}