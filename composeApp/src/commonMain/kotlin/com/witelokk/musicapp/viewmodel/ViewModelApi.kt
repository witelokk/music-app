package com.witelokk.musicapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.witelokk.musicapp.loge
import com.witelokk.musicapp.api.infrastructure.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

fun ViewModel.launchCatching(
    tag: String = "API",
    action: String = "request",
    onError: (Throwable) -> Unit = {},
    block: suspend () -> Unit,
) = viewModelScope.launch {
    try {
        block()
    } catch (throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }
        loge(tag, "$action failed: ${throwable::class.simpleName}: ${throwable.message ?: "unknown error"}")
        onError(throwable)
    }
}

suspend fun <T> runApiCatching(
    tag: String = "API",
    action: String = "request",
    onError: (Throwable) -> Unit = {},
    block: suspend () -> T,
): T? = try {
    block()
} catch (throwable: Throwable) {
    if (throwable is CancellationException) {
        throw throwable
    }
    loge(tag, "$action failed: ${throwable::class.simpleName}: ${throwable.message ?: "unknown error"}")
    onError(throwable)
    null
}

fun HttpResponse<*>.logIfFailure(action: String, tag: String = "API"): Boolean {
    if (!success) {
        loge(tag, "$action failed with status $status")
        return true
    }
    return false
}
