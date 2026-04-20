package com.witelokk.musicapp.auth

import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

class AuthHttpHandler(
    private val authStore: AuthStore,
    private val tokenRefresher: TokenRefresher,
) {
    suspend fun executeWithAuth(
        request: HttpRequestBuilder,
        execute: suspend (HttpRequestBuilder) -> HttpClientCall,
    ): HttpClientCall {
        val accessToken = authStore.currentAccessToken

        request.headers.remove(HttpHeaders.Authorization)
        if (accessToken.isNotBlank()) {
            request.headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        val originalCall = execute(request)

        if (originalCall.response.status != HttpStatusCode.Unauthorized ||
            request.headers["X-Retry"] == "true"
        ) {
            return originalCall
        }

        return when (val refreshResult = tokenRefresher.refreshTokens()) {
            is RefreshResult.Success -> {
                val newRequest = HttpRequestBuilder().takeFrom(request)
                newRequest.headers.remove(HttpHeaders.Authorization)
                newRequest.headers.append(
                    HttpHeaders.Authorization,
                    "Bearer ${refreshResult.accessToken}"
                )
                newRequest.headers.append("X-Retry", "true")
                execute(newRequest)
            }

            RefreshResult.NoInternet,
            RefreshResult.LoggedOut -> originalCall
        }
    }
}
