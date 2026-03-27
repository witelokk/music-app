package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.Auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationScreenState(
    var isEmailInvalid: Boolean = false,
    var isVerificationCodeSent: Boolean = false,
    var verificationCodeRequestFailed: Boolean = false,
    var registrationFailed: Boolean = false,
    var isAuthorized: Boolean = false,
    var isCodeInvalid: Boolean = false,
)

class RegistrationScreenViewModel(
    private val auth: Auth,
) : ViewModel() {
    private val _state = MutableStateFlow(RegistrationScreenState())
    val state = _state.asStateFlow()

    fun registerAndSignIn(name: String, email: String, code: String) {
        viewModelScope.launch {
            try {
                auth.createUserAndSignIn(name, email, code)
            } catch (e: Auth.Errors.InvalidCode) {
                return@launch _state.update { it.copy(isCodeInvalid = true) }
            } catch (e: Exception) {
                return@launch _state.update {
                    it.copy(
                        registrationFailed = true,
                    )
                }
            }

            _state.update {
                it.copy(isAuthorized = true)
            }
        }
    }


    fun sendVerificationCode(email: String) {
        if (!validateEmail(email)) {
            _state.update { it.copy(isEmailInvalid = true) }
            return
        }

        viewModelScope.launch {
            try {
                auth.verificationCodeRequestPost(email)
            } catch (e: Exception) {
                _state.update { it.copy(verificationCodeRequestFailed = true) }
                return@launch
            }

            _state.update { it.copy(isVerificationCodeSent = true) }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return Regex("\"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$\"").matches(email)
    }

    fun clearState() {
        _state.update { RegistrationScreenState() }
    }
}