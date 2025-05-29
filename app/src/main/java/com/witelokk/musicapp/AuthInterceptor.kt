package com.witelokk.musicapp

import android.content.SharedPreferences
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.models.TokensRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sharedPreferences: SharedPreferences,
    private val authApi: AuthApi
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val accessToken = sharedPreferences.getString("access_token", "") ?: ""
        request = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = chain.proceed(request)

        if (response.code != 401) {
            return response
        }

        response.close()

        val refreshToken = sharedPreferences.getString("refresh_token", "") ?: ""
        val tokensResponse = runBlocking {
            authApi.tokensPost(
                TokensRequest(
                    grantType = "refresh_token",
                    refreshToken = refreshToken
                )
            )
        }

        if (!tokensResponse.success) {
            return response
        }

        runBlocking {
            tokensResponse.body().let { tokens ->
                sharedPreferences.edit()
                    .putString("access_token", tokens.accessToken)
                    .putString("refresh_token", tokens.refreshToken)
                    .apply()
            }
        }

        val newAccessToken = sharedPreferences.getString("access_token", "") ?: ""
        val newRequest = request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()

        return chain.proceed(newRequest)
    }
}
