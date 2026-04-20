package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.auth.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginVerificationScreenState(
    var isCodeInvalid: Boolean = false,
    var signInFailed: Boolean = false,
    val isAuthorized: Boolean = false,
    val isButtonEnabled: Boolean = false,
    val isUserNotFound: Boolean = false,
)

class LoginVerificationScreenViewModel(
    private val authSession: AuthSession,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginVerificationScreenState())

    val state = _state.asStateFlow()

    fun signIn(email: String, code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isButtonEnabled = false) }

            try {
                authSession.signIn(email, code)
                _state.update {
                    it.copy(isAuthorized = true)
                }
            } catch (e: AuthSession.Errors.InvalidCode) {
                _state.update { it.copy(isCodeInvalid = true, isButtonEnabled = true) }
            } catch (e: AuthSession.Errors.InvalidUser) {
                _state.update { it.copy(isUserNotFound = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        signInFailed = true,
                        isButtonEnabled = true
                    )
                }
            }
        }
    }

    fun clearState() {
        _state.update { LoginVerificationScreenState() }
    }
}
