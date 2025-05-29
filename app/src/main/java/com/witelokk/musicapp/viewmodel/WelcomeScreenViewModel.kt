package com.witelokk.musicapp.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.models.TokensRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

data class WelcomeScreenState(
    var isCheckingAuthorization: Boolean = true,
    var isAuthorized: Boolean = false,
    var signInFailed: Boolean = false,
)

class WelcomeScreenViewModel(
    application: Application,
    private val authApi: AuthApi,
    private val sharedPreferences: SharedPreferences,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(WelcomeScreenState())

    val state = _state.asStateFlow()

    init {
        checkAuthorization()
    }

    private fun checkAuthorization() {
        val accessToken = sharedPreferences.getString("access_token", null)
        if (accessToken != null) {
            _state.update {
                it.copy(isAuthorized = true)
            }
        }
        _state.update {
            it.copy(isCheckingAuthorization = false)
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            val nonce = generateNonce()

            val signInWithGoogleOption: GetSignInWithGoogleOption =
                GetSignInWithGoogleOption.Builder("408356281538-1vldoc1rlnrpp0pkm9d1gkec4k7csfdk.apps.googleusercontent.com")
                    .setNonce(nonce)
                    .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val credentialManager = CredentialManager.create(getApplication())

            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = getApplication(),
                )
                handleCredentialResult(result)
            } catch (e: Exception) {
                _state.update {
                    it.copy(signInFailed = true)
                }
            }
        }
    }

    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
        return hashedNonce
    }

    private suspend fun handleCredentialResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        signIn(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                        _state.update {
                            it.copy(signInFailed = true)
                        }
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential")
                    _state.update {
                        it.copy(signInFailed = true)
                    }
                }
            }
        }
    }

    private suspend fun signIn(googleIdToken: String) {
        val response = authApi.tokensPost(
            TokensRequest(
                grantType = "google_token",
                googleToken = googleIdToken,
            )
        )

        if (!response.success) {
            _state.update {
                it.copy(signInFailed = true)
            }
            return
        }

        sharedPreferences.edit()
            .putString("access_token", response.body().accessToken)
            .apply()

        _state.update {
            it.copy(isAuthorized = true)
        }
    }
}