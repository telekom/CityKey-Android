package com.telekom.citykey.domain.repository.interceptors

import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.domain.security.crypto.CryptoKeys
import com.telekom.citykey.utils.PreferencesHelper
import okhttp3.Interceptor
import okhttp3.Response

class UserInterceptor(
    private val crypto: Crypto,
    private val preferencesHelper: PreferencesHelper
) : Interceptor {
    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val userServicesMode = if (preferencesHelper.isPreviewMode) "PREVIEW" else "LIVE"
        return chain.proceed(
            chain.request().newBuilder()
                .header("User-Id", crypto.get(CryptoKeys.USER_ID_KEY) ?: "-1")
                .header("Mode", userServicesMode)
                .build()
        )
    }
}
