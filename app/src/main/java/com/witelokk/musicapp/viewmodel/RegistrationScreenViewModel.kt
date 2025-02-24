package com.witelokk.musicapp.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
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
    application: Application,
    private val auth: Auth,
) : AndroidViewModel(application) {
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
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearState() {
        _state.update { RegistrationScreenState() }
    }
}