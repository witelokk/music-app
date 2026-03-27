package com.witelokk.musicapp

import cocoapods.GoogleSignIn.GIDSignIn
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IosGoogleSignIn : GoogleSignIn {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun signIn(
        signIn: (String) -> Unit,
        onSingInFailed: () -> Unit
    ) {
        suspendCoroutine<Unit> { continuation ->
            val rootViewController =
                UIApplication.sharedApplication.keyWindow?.rootViewController

            if (rootViewController == null) {
                onSingInFailed()
                continuation.resume(Unit)
                return@suspendCoroutine
            }

            GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { gidSignInResult, nsError ->
                nsError?.let { println("Error while signing in with Google: $it") }

                val idToken = gidSignInResult?.user?.idToken?.tokenString

                if (idToken != null) {
                    signIn(idToken)
                } else {
                    onSingInFailed()
                }

                continuation.resume(Unit)
            }
        }
    }
}

