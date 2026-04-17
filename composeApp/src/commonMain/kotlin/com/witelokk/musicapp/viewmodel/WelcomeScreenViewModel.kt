package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.DEFAULT_BASE_URL
import com.witelokk.musicapp.GoogleSignIn
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.models.TokensRequest
import com.witelokk.musicapp.logd
import com.witelokk.musicapp.loge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WelcomeScreenState(
    var isCheckingAuthorization: Boolean = true,
    var isAuthorized: Boolean = false,
    var signInFailed: Boolean = false,
    val serverUrlInput: String = DEFAULT_BASE_URL,
    val showServerSettings: Boolean = false,
)

class WelcomeScreenViewModel(
    private val authApi: AuthApi,
    private val settingsRepository: SettingsRepository,
    private val googleSignIn: GoogleSignIn,
) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeScreenState())
    val state = _state.asStateFlow()

    private var logoTapCount: Int = 0

    init {
        viewModelScope.launch { checkAuthorization() }
        viewModelScope.launch { loadServerUrl() }
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

    private suspend fun loadServerUrl() {
        val url = settingsRepository.serverUrl.first()
        _state.update { it.copy(serverUrlInput = url) }
    }

    fun onLogoTapped() {
        logoTapCount++
        if (logoTapCount >= 7 && !_state.value.showServerSettings) {
            _state.update { it.copy(showServerSettings = true) }
        }
    }

    fun onServerUrlChanged(newValue: String) {
        _state.update { it.copy(serverUrlInput = newValue) }
    }

    suspend fun commitServerUrl() {
        authApi.baseUrl = state.value.serverUrlInput
        settingsRepository.setServerUrl(state.value.serverUrlInput)
    }

    fun signInWithGoogle() = viewModelScope.launch {
        googleSignIn.signIn(
            signIn = { it ->
                launchCatching(tag = "GOOGLE_SIGN_IN", action = "exchange Google sign-in token", onError = { error ->
                    loge("GOOGLE_SIGN_IN", error.toString())
                    _state.update { state -> state.copy(signInFailed = true) }
                }) {
                    this@WelcomeScreenViewModel.signIn(it)
                }
            },
            onSingInFailed = {
                _state.update { it.copy(signInFailed = true) }
            }
        )
    }

    private suspend fun signIn(googleIdToken: String) {
        val response = runApiCatching(tag = "GOOGLE_SIGN_IN", action = "request app tokens with Google token", onError = {
            _state.update { state -> state.copy(signInFailed = true) }
        }) {
            authApi.tokensPost(
            TokensRequest(
                grantType = "google_token",
                googleToken = googleIdToken,
            )
            )
        } ?: return

        if (response.logIfFailure("request app tokens with Google token", tag = "GOOGLE_SIGN_IN")) {
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
