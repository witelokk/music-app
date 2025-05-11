package com.witelokk.musicapp

import android.content.ComponentName
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.witelokk.musicapp.api.apis.ArtistsApi
import com.witelokk.musicapp.api.apis.AuthApi
import com.witelokk.musicapp.api.apis.FavoritesApi
import com.witelokk.musicapp.api.apis.PlaylistsApi
import com.witelokk.musicapp.api.apis.ReleasesApi
import com.witelokk.musicapp.api.apis.SearchApi
import com.witelokk.musicapp.api.apis.UsersApi
import com.witelokk.musicapp.viewmodel.LoginScreenViewModel
import com.witelokk.musicapp.viewmodel.SettingsScreenViewModel
import com.witelokk.musicapp.viewmodel.ThemeViewModel
import com.witelokk.musicapp.viewmodel.LoginVerificationScreenViewModel
import com.witelokk.musicapp.viewmodel.RegistrationScreenViewModel
import com.witelokk.musicapp.viewmodel.RegistrationVerificationScreenViewModel
import com.witelokk.musicapp.viewmodel.HomeScreenViewModel
import com.witelokk.musicapp.viewmodel.ArtistScreenViewModel
import com.witelokk.musicapp.viewmodel.WelcomeScreenViewModel
import com.witelokk.musicapp.viewmodel.FavoritesScreenViewModel
import com.witelokk.musicapp.viewmodel.PlaylistReleaseScreenViewModel
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.util.UUID

class CustomHttpLogger : Logger {
    override fun log(message: String) {
        Log.d("API", message)
    }
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

val customSerializersModule = SerializersModule {
    contextual(UUID::class, UUIDSerializer)
//    contextual(Instant::class, DateTimeSerializer)
}

val json = Json {
    serializersModule = customSerializersModule
    prettyPrint = true
    isLenient = true
}

fun HttpClientConfig<*>.default() {
    install(Logging) {
        logger = CustomHttpLogger()
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(json)
    }
}

@OptIn(UnstableApi::class)
val appModule = module {
    val baseUrl = "https://music.witelokk.ru/"
//    val baseUrl = "http://10.0.2.2:8080/"

    single { json }

    single {
        androidApplication().getSharedPreferences("prefs", 0)
    }

    single {
        val sessionToken =
            SessionToken(get(), ComponentName(get(), PlayerSessionService::class.java))
        MediaController.Builder(get(), sessionToken).buildAsync()
    }

    single {
        val dataSourceFactory = DefaultHttpDataSource.Factory().apply {
            setDefaultRequestProperties(
                mapOf(
                    "Authorization" to "Bearer "
                            + (get<SharedPreferences>().getString("access_token", "") ?: "")
                )
            )
        }
        val factory = HlsMediaSource.Factory(dataSourceFactory)
        ExoPlayer.Builder(get()).setMediaSourceFactory(factory).build()
    }

    single {
        MediaSession.Builder(get(), get<ExoPlayer>()).build()
    }

    single {
        MusicPlayer(get())
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

    single {
        val searchApi = SearchApi(baseUrl, httpClientConfig = {
            it.default()
        })
        searchApi.setBearerToken(get<SharedPreferences>().getString("access_token", "") ?: "")
        searchApi
    }

    single {
        val api = ArtistsApi(baseUrl, httpClientConfig = {
            it.default()
        })
        api.setBearerToken(get<SharedPreferences>().getString("access_token", "") ?: "")
        api
    }

    single {
        val api = FavoritesApi(baseUrl, httpClientConfig = {
            it.default()
        })
        api.setBearerToken(get<SharedPreferences>().getString("access_token", "") ?: "")
        api
    }


    single {
        val api = PlaylistsApi(baseUrl, httpClientConfig = {
            it.default()
        })
        api.setBearerToken(get<SharedPreferences>().getString("access_token", "") ?: "")
        api
    }

    single {
        val api = FavoritesApi(baseUrl, httpClientConfig = {
            it.default()
        })
        api.setBearerToken(get<SharedPreferences>().getString("access_token", "") ?: "")
        api
    }

    single {
        val api = ReleasesApi(baseUrl, httpClientConfig = {
            it.default()
        })
        api.setBearerToken(get<SharedPreferences>().getString("access_token", "") ?: "")
        api
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

    single {
        ThemeViewModel(get())
    }
}
