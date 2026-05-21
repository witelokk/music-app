package com.witelokk.musicapp.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NoOpMediaCache : MediaCache {
    override fun cache(url: String) {}

    override fun getCacheState(url: String): StateFlow<MediaCacheState> {
        return MutableStateFlow(MediaCacheState.NOT_CACHED)
    }
}
