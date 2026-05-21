package com.witelokk.musicapp.auth

import com.witelokk.musicapp.api.models.Error
import io.ktor.http.HttpStatusCode
import io.ktor.util.reflect.TypeInfo

class AuthSession(
    private val authStore: AuthStore,
    private val authApiService: AuthApiService,
) {
    companion object Errors {
        class InvalidCode : Exception("Invalid code")
        class InvalidUser : Exception("Invalid user")
        class UserAlreadyExists : Exception("User already exists")
    }

    suspend fun signIn(email: String, code: String) {
        val response = authApiService.signIn(email, code)

        if (response.status == HttpStatusCode.BadRequest.value) {
            val errorResponse = response.typedBody<Error>(TypeInfo(Error::class))

            if (errorResponse.error == "invalid_code") {
                throw InvalidCode()
            } else if (errorResponse.error == "invalid_user") {
                throw InvalidUser()
            }
        }

        if (!response.success) {
            error(response)
        }

        val tokens = response.body()
        authStore.saveTokens(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            accountEmail = email
        )
    }

    suspend fun signInWithGoogle(googleIdToken: String) {
        val response = authApiService.signInWithGoogle(googleIdToken)

        if (!response.success) {
            error(response)
        }

        val tokens = response.body()
        authStore.saveTokens(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    suspend fun createUserAndSignIn(name: String, email: String, code: String) {
        val response = authApiService.createUser(name, email, code)

        if (response.status == HttpStatusCode.Conflict.value) {
            throw UserAlreadyExists()
        }

        if (!response.success) {
            error(response)
        }

        signIn(email, code)
        authStore.saveAccountName(name)
    }

    suspend fun requestVerificationCode(email: String) {
        val response = authApiService.requestVerificationCode(email)

        if (!response.success && response.status != HttpStatusCode.TooManyRequests.value) {
            error(response)
        }
    }

    suspend fun logout() {
        authStore.clearAuth()
    }
}
