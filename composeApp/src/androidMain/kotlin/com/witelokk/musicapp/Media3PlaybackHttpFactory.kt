package com.witelokk.musicapp

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import com.witelokk.musicapp.auth.AuthStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class Media3PlaybackHttpFactory(
    authStore: AuthStore,
) {
    val factory = DefaultHttpDataSource.Factory()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            authStore.state.collectLatest { authState ->
                factory.setDefaultRequestProperties(
                    if (authState.accessToken.isBlank()) {
                        emptyMap()
                    } else {
                        mapOf("Authorization" to "Bearer ${authState.accessToken}")
                    }
                )
            }
        }
    }
}
