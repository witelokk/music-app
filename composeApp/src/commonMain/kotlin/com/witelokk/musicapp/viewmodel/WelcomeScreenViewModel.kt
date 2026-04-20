package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.DEFAULT_BASE_URL
import com.witelokk.musicapp.GoogleSignIn
import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.auth.AuthSession
import com.witelokk.musicapp.auth.AuthStore
import com.witelokk.musicapp.loge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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
    private val authSession: AuthSession,
    private val authStore: AuthStore,
    private val settingsRepository: SettingsRepository,
    private val googleSignIn: GoogleSignIn,
) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeScreenState())
    val state = _state.asStateFlow()

    private var logoTapCount: Int = 0

    init {
        observeAuthorization()
        viewModelScope.launch { loadServerUrl() }
    }

    private fun observeAuthorization() {
        viewModelScope.launch {
            authStore.state.collect { authState ->
                _state.update {
                    it.copy(
                        isAuthorized = authState.isAuthorized,
                        isCheckingAuthorization = false
                    )
                }
            }
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
        runApiCatching(tag = "GOOGLE_SIGN_IN", action = "request app tokens with Google token", onError = {
            _state.update { state -> state.copy(signInFailed = true) }
        }) {
            authSession.signInWithGoogle(googleIdToken)
        }
    }
}
