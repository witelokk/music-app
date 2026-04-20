package com.witelokk.musicapp.auth

import com.witelokk.musicapp.loge
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException

class TokenRefresher(
    private val authStore: AuthStore,
    private val authApiService: AuthApiService,
) {
    private val refreshMutex = Mutex()

    suspend fun refreshTokens(): RefreshResult = refreshMutex.withLock {
        val refreshToken = authStore.state.value.refreshToken

        if (refreshToken.isBlank()) {
            authStore.clearAuth()
            return RefreshResult.LoggedOut
        }

        val response = try {
            authApiService.refresh(refreshToken)
        } catch (error: Throwable) {
            if (error.isOfflineError()) {
                loge("AUTH", "Token refresh skipped because network is unavailable: ${error.message}")
                return RefreshResult.NoInternet
            }

            loge("AUTH", "Token refresh failed: ${error.message}")
            authStore.clearAuth()
            return RefreshResult.LoggedOut
        }

        if (!response.success) {
            authStore.clearAuth()
            return RefreshResult.LoggedOut
        }

        val tokens = response.body()
        authStore.saveTokens(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
        RefreshResult.Success(tokens.accessToken)
    }
}

sealed interface RefreshResult {
    data class Success(val accessToken: String) : RefreshResult
    data object NoInternet : RefreshResult
    data object LoggedOut : RefreshResult
}

private fun Throwable.isOfflineError(): Boolean {
    return this is IOException ||
        this is UnresolvedAddressException ||
        this is SocketTimeoutException ||
        this is ConnectTimeoutException ||
        this is HttpRequestTimeoutException
}
