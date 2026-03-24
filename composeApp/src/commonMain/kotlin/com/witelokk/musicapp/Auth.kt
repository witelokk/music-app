package com.witelokk.musicapp

import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.apis.UsersApi
import com.witelokk.musicapp.api.models.CreateUserRequest
import com.witelokk.musicapp.api.models.FailureResponse
import com.witelokk.musicapp.api.models.TokensRequest
import com.witelokk.musicapp.api.models.VerificationCodeRequest
import io.ktor.http.HttpStatusCode
import io.ktor.util.reflect.TypeInfo

class Auth(
    private val authApi: AuthApi,
    private val usersApi: UsersApi,
    private val settingsRepository: SettingsRepository,
) {
    companion object Errors {
        class InvalidCode : Exception("Invalid code")
        class InvalidUser : Exception("Invalid user")
        class UserAlreadyExists : Exception("Invalid user")
    }

    suspend fun signIn(email: String, code: String) {
        val response = authApi.tokensPost(
            TokensRequest(
                grantType = "code",
                email = email,
                code = code
            )
        )

        if (response.status == HttpStatusCode.BadRequest.value) {
            val errorResponse = response.typedBody<FailureResponse>(
                TypeInfo(FailureResponse::class)
            )

            if (errorResponse.error == "invalid_code") {
                throw InvalidCode()
            } else if (errorResponse.error == "invalid_user") {
                throw InvalidUser()
            }
        }

        if (!response.success) {
            error(response)
        }

        settingsRepository.setAccessToken(response.body().accessToken)
        settingsRepository.setRefreshToken(response.body().refreshToken)
        settingsRepository.setAccountEmail(email)
    }

    suspend fun createUserAndSignIn(name: String, email: String, code: String) {
        val response = usersApi.usersPost(
            CreateUserRequest(
                name = name,
                email = email,
                code = code,
            )
        )

        if (response.status == HttpStatusCode.Conflict.value) {
            throw UserAlreadyExists()
        }

        if (!response.success) {
            error(response)
        }

        signIn(email, code)
    }

    suspend fun verificationCodeRequestPost(email: String) {
        val response =
            authApi.verificationCodeRequestPost(VerificationCodeRequest(email))

        if (!response.success and (response.status != HttpStatusCode.TooManyRequests.value)) {
            error(response)
        }
    }
}
