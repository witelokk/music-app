package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.GoogleSignIn
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.models.TokensRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WelcomeScreenState(
    var isCheckingAuthorization: Boolean = true,
    var isAuthorized: Boolean = false,
    var signInFailed: Boolean = false,
)

class WelcomeScreenViewModel(
    private val authApi: AuthApi,
    private val settingsRepository: SettingsRepository,
    private val googleSignIn: GoogleSignIn,
) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeScreenState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch { checkAuthorization() }
    }

    private suspend fun checkAuthorization() {
        val accessToken = settingsRepository.accessToken.first()
        if (accessToken.isNotBlank()) {
            _state.update {
                it.copy(isAuthorized = true)
            }
        }
        _state.update {
            it.copy(isCheckingAuthorization = false)
        }
    }

    fun signInWithGoogle() = viewModelScope.launch {
        googleSignIn.signIn(
            signIn = { viewModelScope.launch { this@WelcomeScreenViewModel.signIn(it) } },
            onSingInFailed = {
                _state.update { it.copy(signInFailed = true) }
            }
        )
    }

    private suspend fun signIn(googleIdToken: String) {
        val response = authApi.tokensPost(
            TokensRequest(
                grantType = "google_token",
                googleToken = googleIdToken,
            )
        )

        if (!response.success) {
            _state.update {
                it.copy(signInFailed = true)
            }
            return
        }

        settingsRepository.setAccessToken(response.body().accessToken)
        settingsRepository.setRefreshToken(response.body().refreshToken)

        _state.update {
            it.copy(isAuthorized = true)
        }
    }
}