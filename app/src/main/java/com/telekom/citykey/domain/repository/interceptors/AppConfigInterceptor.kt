package com.telekom.citykey.domain.repository.interceptors

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.domain.notifications.TpnsManager
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*

class AppConfigInterceptor : Interceptor {

    companion object {
        val acceptedLanguages = listOf("en", "de", "tr")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val localeLanguage = Locale.getDefault().language.lowercase()
        val acceptedLanguage = if (acceptedLanguages.contains(localeLanguage)) localeLanguage else "en"
        return chain.proceed(
            chain.request().newBuilder()
                .addHeader("Accept-Language", acceptedLanguage)
                .addHeader("OS-Name", "Android")
                .addHeader("App-Version", BuildConfig.SERVICES_VERSION)
                .addHeader("Requesting-App", "CITYKEY")
                .addHeader("Push-Id", TpnsManager.pushId)
                .build()
        )
    }
}
