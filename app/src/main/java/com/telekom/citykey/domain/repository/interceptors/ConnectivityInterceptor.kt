package com.telekom.citykey.domain.repository.interceptors

import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.ConnectException
import java.net.UnknownHostException

class ConnectivityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (exception: Exception) {
            throw if (exception is ConnectException ||
                exception is UnknownHostException
            ) NoConnectionException() else exception
        }
    }
}
