package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import com.witelokk.musicapp.api.apis.CompatAuthApi
import com.witelokk.musicapp.api.models.SendVerificationEmailRequest
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginScreenState(
    var isEmailInvalid: Boolean = false,
    var isVerificationCodeSent: Boolean = false,
    var verificationCodeRequestFailed: Boolean = false,
)

class LoginScreenViewModel(private val authApi: CompatAuthApi) : ViewModel() {
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
            val response = authApi.createVerificationCodeRequest(
                SendVerificationEmailRequest(email)
            )

            if (!response.success && response.status != HttpStatusCode.TooManyRequests.value) {
                response.logIfFailure("send verification code for $email")
                _state.update { it.copy(verificationCodeRequestFailed = true) }
                return@launchCatching
            }

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
