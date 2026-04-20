package com.witelokk.musicapp.cache

import com.witelokk.musicapp.api.models.HomeFeed
import kotlinx.coroutines.flow.Flow

interface HomeCache {
    fun observeLayout(): Flow<HomeFeed?>
    suspend fun cache(layout: HomeFeed)
}
