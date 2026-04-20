package com.witelokk.musicapp.auth

data class AuthState(
    val accountName: String = "",
    val accountEmail: String = "",
    val accessToken: String = "",
    val refreshToken: String = "",
) {
    val isAuthorized: Boolean
        get() = accessToken.isNotBlank()
}
