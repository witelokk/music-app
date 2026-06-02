package com.witelokk.musicapp

import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.infrastructure.HttpResponse
import com.witelokk.musicapp.api.infrastructure.RequestConfig
import com.witelokk.musicapp.api.infrastructure.RequestMethod
import com.witelokk.musicapp.api.infrastructure.wrap
import com.witelokk.musicapp.api.models.GetTokensByCodeRequest
import com.witelokk.musicapp.api.models.GetTokensByGoogleTokenRequest
import com.witelokk.musicapp.api.models.GetTokensByRefreshTokenRequest
import com.witelokk.musicapp.api.models.TokensResponse
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json

class CompatAuthApi : AuthApi {

    constructor(
        baseUrl: String = BASE_URL,
        httpClientEngine: HttpClientEngine? = null,
        httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
        jsonSerializer: Json = JSON_DEFAULT
    ) : super(
        baseUrl = baseUrl,
        httpClientEngine = httpClientEngine,
        httpClientConfig = httpClientConfig,
        jsonSerializer = jsonSerializer
    )

    constructor(
        baseUrl: String,
        httpClient: HttpClient
    ) : super(baseUrl = baseUrl, httpClient = httpClient)

    suspend fun generateTokens(getTokensRequest: GetTokensByCodeRequest): HttpResponse<TokensResponse> =
        generateTokensRequest(getTokensRequest)

    suspend fun generateTokens(getTokensRequest: GetTokensByGoogleTokenRequest): HttpResponse<TokensResponse> =
        generateTokensRequest(getTokensRequest)

    suspend fun generateTokens(getTokensRequest: GetTokensByRefreshTokenRequest): HttpResponse<TokensResponse> =
        generateTokensRequest(getTokensRequest)

    private suspend fun generateTokensRequest(body: Any): HttpResponse<TokensResponse> {
        val requestConfig = RequestConfig<Any?>(
            method = RequestMethod.POST,
            path = "/tokens",
            query = mutableMapOf(),
            headers = mutableMapOf(),
            requiresAuthentication = false,
        )

        return jsonRequest(
            requestConfig = requestConfig,
            body = body,
            authNames = emptyList()
        ).wrap()
    }
}