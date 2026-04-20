package com.witelokk.musicapp.auth

import com.witelokk.musicapp.SettingsRepository
import com.witelokk.musicapp.api.apis.CompatAuthApi
import com.witelokk.musicapp.api.infrastructure.HttpResponse
import com.witelokk.musicapp.api.models.CreateUserRequest
import com.witelokk.musicapp.api.models.GetTokensByCodeRequest
import com.witelokk.musicapp.api.models.GetTokensByGoogleTokenRequest
import com.witelokk.musicapp.api.models.GetTokensByRefreshTokenRequest
import com.witelokk.musicapp.api.models.SendVerificationEmailRequest
import com.witelokk.musicapp.api.models.TokensResponse
import kotlinx.coroutines.flow.first

class AuthApiService(
    private val settingsRepository: SettingsRepository,
) {
    suspend fun signIn(email: String, code: String): HttpResponse<TokensResponse> {
        return authApi().generateTokens(
            GetTokensByCodeRequest(
                grantType = GetTokensByCodeRequest.GrantType.code,
                email = email,
                code = code
            )
        )
    }

    suspend fun signInWithGoogle(googleIdToken: String): HttpResponse<TokensResponse> {
        return authApi().generateTokens(
            GetTokensByGoogleTokenRequest(
                grantType = GetTokensByGoogleTokenRequest.GrantType.google_token,
                googleToken = googleIdToken,
            )
        )
    }

    suspend fun refresh(refreshToken: String): HttpResponse<TokensResponse> {
        return authApi().generateTokens(
            GetTokensByRefreshTokenRequest(
                grantType = GetTokensByRefreshTokenRequest.GrantType.refresh_token,
                refreshToken = refreshToken
            )
        )
    }

    suspend fun createUser(name: String, email: String, code: String) =
        authApi().createUser(
            CreateUserRequest(
                name = name,
                email = email,
                code = code,
            )
        )

    suspend fun requestVerificationCode(email: String) =
        authApi().createVerificationCodeRequest(SendVerificationEmailRequest(email))

    private suspend fun authApi(): CompatAuthApi {
        val baseUrl = settingsRepository.serverUrl.first()
        return CompatAuthApi(baseUrl, httpClientConfig = {})
    }
}
