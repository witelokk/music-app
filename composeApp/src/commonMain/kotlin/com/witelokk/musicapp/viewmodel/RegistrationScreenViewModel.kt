package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.auth.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationScreenState(
    var isEmailInvalid: Boolean = false,
    var isVerificationCodeSent: Boolean = false,
    var verificationCodeRequestFailed: Boolean = false,
    var verificationCodeRequestRateLimited: Boolean = false,
    var registrationFailed: Boolean = false,
    var isAuthorized: Boolean = false,
    var isCodeInvalid: Boolean = false,
)

class RegistrationScreenViewModel(
    private val authSession: AuthSession,
) : ViewModel() {
    private val _state = MutableStateFlow(RegistrationScreenState())
    val state = _state.asStateFlow()

    fun registerAndSignIn(name: String, email: String, code: String) {
        viewModelScope.launch {
            try {
                authSession.createUserAndSignIn(name, email, code)
            } catch (e: AuthSession.Errors.InvalidCode) {
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

        _state.update { it.copy(isEmailInvalid = false) }

        viewModelScope.launch {
            try {
                authSession.requestVerificationCode(email)
            } catch (e: AuthSession.Errors.TooManyVerificationRequests) {
                _state.update { it.copy(verificationCodeRequestRateLimited = true) }
                return@launch
            } catch (e: Exception) {
                _state.update { it.copy(verificationCodeRequestFailed = true) }
                return@launch
            }

            _state.update { it.copy(isVerificationCodeSent = true) }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matches(email)
    }

    fun clearState() {
        _state.update { RegistrationScreenState() }
    }
}
