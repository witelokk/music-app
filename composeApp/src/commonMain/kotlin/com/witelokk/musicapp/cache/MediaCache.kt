package com.witelokk.musicapp.cache

import kotlinx.coroutines.flow.StateFlow

enum class MediaCacheState {
    NOT_CACHED,
    IN_PROGRESS,
    CACHED,
    FAILED,
}

interface MediaCache {
    fun cache(url: String)
    fun getCacheState(url: String): StateFlow<MediaCacheState>
}
