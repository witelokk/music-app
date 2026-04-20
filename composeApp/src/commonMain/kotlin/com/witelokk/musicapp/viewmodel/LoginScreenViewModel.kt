package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import com.witelokk.musicapp.auth.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginScreenState(
    var isEmailInvalid: Boolean = false,
    var isVerificationCodeSent: Boolean = false,
    var verificationCodeRequestFailed: Boolean = false,
)

class LoginScreenViewModel(private val authSession: AuthSession) : ViewModel() {
    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    fun sendVerificationCode(email: String) {
        if (!validateEmail(email)) {
            _state.update { it.copy(isEmailInvalid = true) }
            return
        }

        launchCatching(action = "send verification code for $email", onError = {
            _state.update { state -> state.copy(verificationCodeRequestFailed = true) }
        }) {
            authSession.requestVerificationCode(email)
            _state.update { it.copy(isVerificationCodeSent = true) }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return Regex("\"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$\"").matches(email)
    }

    fun clearState() {
        _state.update { LoginScreenState() }
    }
}
