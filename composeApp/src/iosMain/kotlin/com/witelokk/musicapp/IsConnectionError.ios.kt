package com.witelokk.musicapp

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut

actual fun Throwable.isConnectionError(): Boolean {
    val nsError = (this as? DarwinHttpRequestException)?.origin
    if (nsError != null) {
        return nsError.domain == NSURLErrorDomain && (nsError.code == NSURLErrorNotConnectedToInternet || nsError.code == NSURLErrorTimedOut || nsError.code == -1004L)
    }
    return when (this) {
        is UnresolvedAddressException -> true
        is SocketTimeoutException -> true
        else -> false
    }
}