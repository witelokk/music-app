package com.witelokk.musicapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.compose.ConnectivityState
import dev.jordond.connectivity.compose.rememberConnectivityState
import io.ktor.client.HttpClient
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import org.koin.compose.koinInject

@Composable
fun rememberHttpConnectivityState(
    settingsRepository: SettingsRepository = koinInject(),
    httpClient: HttpClient = koinInject(),
): ConnectivityState {
    val scope = rememberCoroutineScope()
    val serverUrl by settingsRepository.serverUrl.collectAsState(initial = DEFAULT_BASE_URL)

    val connectivity = remember(serverUrl, httpClient, scope) {
        val url = Url(serverUrl)
        Connectivity(scope = scope, httpClient = httpClient) {
            autoStart = true
            url("$serverUrl/health")
            port = when {
                url.port > 0 -> url.port
                url.protocol == URLProtocol.HTTPS -> 443
                else -> 80
            }
            pollingIntervalMs = 5.seconds
            timeoutMs = 2.seconds
        }
    }

    return rememberConnectivityState(connectivity = connectivity, scope = scope)
}
