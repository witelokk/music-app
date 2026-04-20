package com.witelokk.musicapp

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

actual fun Throwable.isConnectionError(): Boolean {
    return when (this) {
        is UnresolvedAddressException -> true
        is UnknownHostException, is SocketTimeoutException, is ConnectException -> true
        else -> false
    }
}