package com.witelokk.musicapp.auth

import com.witelokk.musicapp.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AuthStore(
    private val settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val state: StateFlow<AuthState> = settingsRepository.authState.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AuthState()
    )

    val currentAccessToken: String
        get() = state.value.accessToken

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        accountEmail: String? = null,
    ) {
        settingsRepository.updateAuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accountEmail = accountEmail
        )
    }

    suspend fun clearAuth() {
        settingsRepository.clearAuth()
    }
}
