package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.Auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationVerificationScreenState(
    var isCodeInvalid: Boolean = false,
    var registrationFailed: Boolean = false,
    val isAuthorized: Boolean = false,
    val isButtonEnabled: Boolean = false,
    val userAlreadyExists: Boolean = false,
)

class RegistrationVerificationScreenViewModel(
    private val auth: Auth,
) : ViewModel() {
    private val _state = MutableStateFlow(RegistrationVerificationScreenState())

    val state = _state.asStateFlow()

    fun registerAndLogin(name: String, email: String, code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isButtonEnabled = false) }

            try {
                auth.createUserAndSignIn(name, email, code)
            } catch (e: Auth.Errors.InvalidCode) {
                return@launch _state.update {
                    it.copy(
                        isCodeInvalid = true,
                        isButtonEnabled = true
                    )
                }
            } catch (e: Auth.Errors.UserAlreadyExists) {
                return@launch _state.update {
                    it.copy(
                        userAlreadyExists = true,
                        isButtonEnabled = true
                    )
                }
            } catch (e: Exception) {
                return@launch _state.update {
                    it.copy(
                        registrationFailed = true,
                        isButtonEnabled = true
                    )
                }
            }

            _state.update {
                it.copy(isAuthorized = true)
            }
        }
    }

    fun singIn(email: String, code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isButtonEnabled = false) }

            try {
                auth.signIn(email, code)
            } catch (e: Auth.Errors.InvalidCode) {
                return@launch _state.update {
                    it.copy(
                        isCodeInvalid = true,
                        isButtonEnabled = true
                    )
                }
            } catch (e: Exception) {
                return@launch _state.update {
                    it.copy(
                        registrationFailed = true,
                        isButtonEnabled = true
                    )
                }
            }

            _state.update {
                it.copy(isAuthorized = true)
            }
        }
    }
}