package com.witelokk.musicapp

interface GoogleSignIn {
    suspend fun signIn(signIn: (String) -> Unit, onSingInFailed: () -> Unit)
}