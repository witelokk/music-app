package com.witelokk.musicapp

import android.util.Log
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.apis.UsersApi
import com.witelokk.musicapp.viewmodel.LoginScreenViewModel
import com.witelokk.musicapp.viewmodel.LoginVerificationScreenViewModel
import com.witelokk.musicapp.viewmodel.RegistrationScreenViewModel
import com.witelokk.musicapp.viewmodel.RegistrationVerificationScreenViewModel
import com.witelokk.musicapp.viewmodel.WelcomeScreenViewModel
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class CustomHttpLogger : Logger {
    override fun log(message: String) {
        Log.d("API", message)
    }
}

fun HttpClientConfig<*>.default() {
    install(Logging) {
        logger = CustomHttpLogger()
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json()
    }
}

val appModule = module {
    val baseUrl = "https://music.witelokk.ru/"

    single {
        androidApplication().getSharedPreferences("prefs", 0)
    }

    single {
        Auth(get(), get(), get())
    }

    single {
        AuthApi(baseUrl, httpClientConfig = {
            it.default()
        })
    }

    single {
        UsersApi(baseUrl, httpClientConfig = {
            it.default()
        })
    }

    viewModelOf(::WelcomeScreenViewModel)
    viewModelOf(::LoginScreenViewModel)
    viewModelOf(::RegistrationScreenViewModel)
    viewModelOf(::LoginVerificationScreenViewModel)
    viewModelOf(::RegistrationVerificationScreenViewModel)
}
