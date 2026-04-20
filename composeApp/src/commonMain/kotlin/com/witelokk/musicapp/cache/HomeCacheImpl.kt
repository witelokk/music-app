package com.witelokk.musicapp.cache

import com.witelokk.musicapp.api.models.HomeFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class HomeCacheImpl(
    database: MusicAppDatabase,
    private val json: Json,
) : HomeCache {
    private val homeFeedDao = database.homeFeedDao()

    override fun observeLayout(): Flow<HomeFeed?> {
        return homeFeedDao.observeFeed().map { layout ->
            layout?.let { json.decodeFromString<HomeFeed>(it.layoutJson) }
        }
    }

    override suspend fun cache(layout: HomeFeed) {
        homeFeedDao.insertFeed(
            HomeFeedEntity(
                layoutJson = json.encodeToString(layout),
            )
        )
    }
}
