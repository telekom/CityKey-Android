/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

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
