package com.witelokk.musicapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.models.ComwitelokkmusicmodelsVerificationCodeRequest
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginScreenState(
    var isEmailInvalid: Boolean = false,
    var isVerificationCodeSent: Boolean = false,
    var verificationCodeRequestFailed: Boolean = false,
)

class LoginScreenViewModel(private val authApi: AuthApi) : ViewModel() {
    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    fun sendVerificationCode(email: String) {
        if (!validateEmail(email)) {
            _state.update { it.copy(isEmailInvalid = true) }
            return
        }

        viewModelScope.launch {
            val response = authApi.verificationCodeRequestPost(
                ComwitelokkmusicmodelsVerificationCodeRequest(
                    email = email
                )
            )

            if (!response.success and (response.status != HttpStatusCode.TooManyRequests.value)) {
                _state.update { it.copy(verificationCodeRequestFailed = true) }
                return@launch
            }

            _state.update { it.copy(isVerificationCodeSent = true) }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearState() {
        _state.update { LoginScreenState() }
    }
}