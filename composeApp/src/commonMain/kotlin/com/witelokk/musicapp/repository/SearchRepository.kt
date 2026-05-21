package com.witelokk.musicapp.repository

import com.witelokk.musicapp.api.models.SearchResponse

interface SearchRepository {
    suspend fun search(query: String, page: Int = 1, limit: Int = 20): SearchResponse
}
