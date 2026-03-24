package com.witelokk.musicapp

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

class AndroidGoogleSignIn(private val context: Context) : GoogleSignIn {
    override suspend fun signIn(
        signIn: (String) -> Unit,
        onSingInFailed: () -> Unit
    ) {
        val nonce = generateNonce()

        val signInWithGoogleOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder("408356281538-bimfjgt9o4shke6oen3bqom6napt63oe.apps.googleusercontent.com")
                .setNonce(nonce)
                .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            handleCredentialResult(result, signIn, onSingInFailed)
        } catch (e: Exception) {
            logd("GOOGLE_SIGN_IN", e.toString())
            onSingInFailed()
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

    private fun handleCredentialResult(
        result: GetCredentialResponse, signIn: (String) -> Unit,
        onSingInFailed: () -> Unit
    ) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        signIn(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("GOOGLE_SIGN_IN", "Received an invalid google id token response", e)
                        onSingInFailed()
                    }
                } else {
                    Log.e("GOOGLE_SIGN_IN", "Unexpected type of credential")
                    onSingInFailed()
                }
            }
        }
    }
}