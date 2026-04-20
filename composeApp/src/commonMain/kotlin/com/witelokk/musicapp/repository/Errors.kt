package com.witelokk.musicapp.repository

class ConnectionErrorException(throwable: Throwable? = null) : Exception(throwable)
class ApiErrorException(statusCode: Int, message: String? = null, throwable: Throwable? = null) : Exception(message, throwable)