package com.witelokk.musicapp

import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.models.TokensRequest
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first

suspend fun handleAuth(
    request: HttpRequestBuilder,
    execute: suspend (HttpRequestBuilder) -> HttpClientCall,
    settingsRepository: SettingsRepository,
    authApi: AuthApi
): HttpClientCall {
    val accessToken = settingsRepository.accessToken.first()

    request.headers.remove(HttpHeaders.Authorization)
    request.headers.append(HttpHeaders.Authorization, "Bearer $accessToken")

    val originalCall = execute(request)

    if (originalCall.response.status != HttpStatusCode.Unauthorized ||
        request.headers["X-Retry"] == "true"
    ) {
        return originalCall
    }

    val refreshToken = settingsRepository.refreshToken.first()

    val tokensResponse = authApi.tokensPost(
        TokensRequest("refresh_token", refreshToken = refreshToken)
    )

    if (!tokensResponse.success) return originalCall

    val tokens = tokensResponse.body()

    settingsRepository.setAccessToken(tokens.accessToken)
    settingsRepository.setRefreshToken(tokens.refreshToken)

    val newRequest = HttpRequestBuilder().takeFrom(request)
    newRequest.headers.remove(HttpHeaders.Authorization)
    newRequest.headers.append(HttpHeaders.Authorization, "Bearer ${tokens.accessToken}")
    newRequest.headers.append("X-Retry", "true")

    return execute(newRequest)
}