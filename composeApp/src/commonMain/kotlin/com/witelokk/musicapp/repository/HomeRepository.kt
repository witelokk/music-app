package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.models.HomeFeed
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeFeed(): Flow<HomeFeed?>
    suspend fun refreshHomeFeed()
}
