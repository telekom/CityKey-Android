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

import com.telekom.citykey.domain.repository.HttpResponseCodes
import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.utils.PreferencesHelper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class OAuth2Interceptor(
    private val tokenProvider: OAuth2TokenManager,
    private val preferencesHelper: PreferencesHelper
) : Interceptor {

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {

        var token = tokenProvider.fetchAccessToken()

        val response = authorizeAndProceed(chain, token, tokenProvider.userIdToken)
        if (response.isSuccessful ||
            response.code != HttpResponseCodes.NOT_AUTHORIZED &&
            response.code != HttpResponseCodes.FORBIDDEN
        ) return response

        try {
            token = tokenProvider.requestNewToken()
            return authorizeAndProceed(chain, token, tokenProvider.userIdToken)
        } catch (t: Throwable) {
            if (t.cause is IOException) throw t as IOException
            throw t
        }
    }

    private fun authorizeAndProceed(chain: Interceptor.Chain, token: String, userId: String): Response {
        val userServicesMode = if (preferencesHelper.isPreviewMode) "PREVIEW" else "LIVE"
        return chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", token)
                .header("User-Id", userId)
                .header("Mode", userServicesMode)
                .build()
        )
    }
}
