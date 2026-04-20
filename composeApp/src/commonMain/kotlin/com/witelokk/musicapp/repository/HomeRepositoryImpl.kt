package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.apis.HomeApi
import com.witelokk.musicapp.api.models.HomeFeed
import com.witelokk.musicapp.cache.HomeCache
import com.witelokk.musicapp.isConnectionError
import com.witelokk.musicapp.loge
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow

class HomeRepositoryImpl(
    private val api: HomeApi,
    private val cache: HomeCache,
) : HomeRepository {

    override fun observeFeed(): Flow<HomeFeed?> {
        return cache.observeLayout()
    }

    override suspend fun refreshHomeFeed() {
        val response = try {
            api.getHomeFeed()
        } catch (e: Exception) {
            if (e.isConnectionError()) {
                throw ConnectionErrorException(e)
            }
            loge("HOME_REPO", e.stackTraceToString())
            return
        }

        if (!response.success) {
            throw ApiErrorException(response.status, response.response.bodyAsText())
        }

        cache.cache(response.body())
    }
}
