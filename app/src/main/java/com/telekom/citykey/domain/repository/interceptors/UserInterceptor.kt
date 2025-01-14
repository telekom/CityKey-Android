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
