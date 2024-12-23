package com.telekom.citykey.common

class NetworkException(
    val code: Int,
    val error: Any?,
    override val message: String = "",
    val throwable: Throwable
) : RuntimeException()
