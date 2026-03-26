package com.witelokk.musicapp

import com.witelokk.musicapp.api.apis.ArtistsApi
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.FollowingsApi
import com.witelokk.musicapp.api.apis.HomeScreenApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.ReleasesApi
import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.api.apis.UsersApi
import com.witelokk.musicapp.viewmodel.ArtistScreenViewModel
import com.witelokk.musicapp.viewmodel.FavoritesScreenViewModel
import com.witelokk.musicapp.viewmodel.HomeScreenViewModel
import com.witelokk.musicapp.viewmodel.LoginScreenViewModel
import com.witelokk.musicapp.viewmodel.LoginVerificationScreenViewModel
import com.witelokk.musicapp.viewmodel.PlaylistReleaseScreenViewModel
import com.witelokk.musicapp.viewmodel.QueueScreenViewModel
import com.witelokk.musicapp.viewmodel.RegistrationScreenViewModel
import com.witelokk.musicapp.viewmodel.RegistrationVerificationScreenViewModel
import com.witelokk.musicapp.viewmodel.SettingsScreenViewModel
import com.witelokk.musicapp.viewmodel.ThemeViewModel
import com.witelokk.musicapp.viewmodel.WelcomeScreenViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class CustomHttpLogger : Logger {
    override fun log(message: String) {
        logd("API", message)
    }
}

expect val platformModule: Module

val appModule = module {
    includes(platformModule)

    val baseUrl = "https://music.witelokk.ru/"

    single {
        Json
    }

    single {
        SettingsRepository(get())
    }

    single {
        MusicPlayer(get())
    }

    single {
        Auth(get(), get(), get())
    }

    factory {
        AuthApi(baseUrl, httpClientConfig = {})
    }

    single {
        val client = HttpClient(get<HttpClientEngineFactory<*>>()) {
            install(Logging) {
                logger = CustomHttpLogger()
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json()
            }
        }
        client.plugin(HttpSend).intercept { request ->
            handleAuth(request, ::execute, get(), get())
        }
        client
    }

    factory {
        UsersApi(baseUrl, get<HttpClient>())
    }

    factory {
        SearchApi(baseUrl, get<HttpClient>())
    }

    factory {
        ArtistsApi(baseUrl, get<HttpClient>())
    }

    factory {
        FavoritesApi(baseUrl, get<HttpClient>())
    }

    factory {
        PlaylistsApi(baseUrl, get<HttpClient>())
    }

    factory {
        FavoritesApi(baseUrl, get<HttpClient>())
    }

    factory {
        ReleasesApi(baseUrl, get<HttpClient>())
    }

    factory {
        HomeScreenApi(baseUrl, get<HttpClient>())
    }

    factory {
        FollowingsApi(baseUrl, get<HttpClient>())
    }

    viewModelOf(::WelcomeScreenViewModel)
    viewModelOf(::LoginScreenViewModel)
    viewModelOf(::RegistrationScreenViewModel)
    viewModelOf(::LoginVerificationScreenViewModel)
    viewModelOf(::RegistrationVerificationScreenViewModel)
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::SettingsScreenViewModel)
    viewModelOf(::ArtistScreenViewModel)
    viewModelOf(::FavoritesScreenViewModel)
    viewModelOf(::PlaylistReleaseScreenViewModel)
    viewModelOf(::QueueScreenViewModel)

    single {
        ThemeViewModel(get())
    }
}

fun initKoin(config: KoinApplication.() -> Unit = {}) {
    startKoin {
        config()
        modules(appModule)
    }
}