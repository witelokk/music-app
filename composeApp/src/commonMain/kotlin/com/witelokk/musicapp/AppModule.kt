package com.witelokk.musicapp

import com.witelokk.musicapp.auth.AuthApiService
import com.witelokk.musicapp.auth.AuthHttpHandler
import com.witelokk.musicapp.auth.AuthSession
import com.witelokk.musicapp.auth.AuthStore
import com.witelokk.musicapp.auth.TokenRefresher
import com.witelokk.musicapp.api.apis.ArtistsApi
import com.witelokk.musicapp.api.apis.CompatAuthApi
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.FollowingsApi
import com.witelokk.musicapp.api.apis.HomeApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.ReleasesApi
import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.cache.FavoritesCache
import com.witelokk.musicapp.cache.FavoritesCacheImpl
import com.witelokk.musicapp.cache.HomeCache
import com.witelokk.musicapp.cache.HomeCacheImpl
import com.witelokk.musicapp.cache.MusicAppDatabase
import com.witelokk.musicapp.cache.PlaylistCache
import com.witelokk.musicapp.cache.PlaylistCacheImpl
import com.witelokk.musicapp.cache.getRoomDatabase
import com.witelokk.musicapp.repository.FavoritesRepository
import com.witelokk.musicapp.repository.FavoritesRepositoryImpl
import com.witelokk.musicapp.repository.HomeRepository
import com.witelokk.musicapp.repository.HomeRepositoryImpl
import com.witelokk.musicapp.repository.PlaylistsRepository
import com.witelokk.musicapp.repository.PlaylistsRepositoryImpl
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
import dev.jordond.connectivity.Connectivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

const val DEFAULT_BASE_URL = "https://music.witelokk.ru/"

val appModule = module {
    includes(platformModule)

    single<Json> {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single {
        SettingsRepository(get())
    }

    single {
        AuthStore(get())
    }

    single {
        AuthApiService(get())
    }

    single {
        TokenRefresher(get(), get())
    }

    single {
        AuthSession(get(), get())
    }

    single {
        AuthHttpHandler(get(), get())
    }

    single {
        MusicPlayer(get())
    }

    single<MusicAppDatabase> {
        getRoomDatabase(get())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        CompatAuthApi(baseUrl, httpClientConfig = {})
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
            get<AuthHttpHandler>().executeWithAuth(request, ::execute)
        }
        client
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        SearchApi(baseUrl, get<HttpClient>())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        ArtistsApi(baseUrl, get<HttpClient>())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        FavoritesApi(baseUrl, get<HttpClient>())
    }

    factory<FavoritesCache> {
        FavoritesCacheImpl(get(), get())
    }

    factory<FavoritesRepository> {
        FavoritesRepositoryImpl(get(), get())
    }

    factory<HomeCache> {
        HomeCacheImpl(get(), get())
    }

    factory<HomeRepository> {
        HomeRepositoryImpl(get(), get())
    }

    factory<PlaylistCache> {
        PlaylistCacheImpl(get(), get())
    }

    factory<PlaylistsRepository> {
        PlaylistsRepositoryImpl(get(), get())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        PlaylistsApi(baseUrl, get<HttpClient>())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        FavoritesApi(baseUrl, get<HttpClient>())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        ReleasesApi(baseUrl, get<HttpClient>())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
        HomeApi(baseUrl, get<HttpClient>())
    }

    factory {
        val settingsRepository: SettingsRepository = get()
        val baseUrl = runBlocking { settingsRepository.serverUrl.first() }
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
