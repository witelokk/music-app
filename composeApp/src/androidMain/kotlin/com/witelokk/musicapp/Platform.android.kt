package com.witelokk.musicapp

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import org.koin.mp.KoinPlatform

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun openAppLanguageSettings(): Boolean {
    val context = KoinPlatform.getKoin().get<android.content.Context>()
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    return runCatching {
        context.startActivity(intent)
    }.isSuccess
}

actual fun isActiveNetworkMetered(): Boolean {
    val context = KoinPlatform.getKoin().get<android.content.Context>()
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    return connectivityManager?.isActiveNetworkMetered ?: true
}

actual fun supportsDynamicColors(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
