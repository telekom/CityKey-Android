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

package com.telekom.citykey.domain.repository

import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.domain.security.crypto.CryptoKeys
import com.telekom.citykey.models.api.requests.RefreshTokenRequest
import com.telekom.citykey.models.user.Credentials
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.view.user.login.LogoutReason
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class OAuth2TokenManager(
    private val tokenApi: SmartCredentialsApi,
    private val crypto: Crypto,
    private val prefs: PreferencesHelper
) {

    companion object {
        var keepMeLoggedIn: Boolean = false
        private const val ACCESS_TOKEN_EXPIRY = "ACCESS_TOKEN_EXPIRATION"
        private const val REFRESH_TOKEN_EXPIRY = "REFRESH_TOKEN_EXPIRATION"
    }

    private var accessTokenExpiry = prefs.sharedPreferences.getLong(ACCESS_TOKEN_EXPIRY, -1)
    private var refreshTokenExpiry = prefs.sharedPreferences.getLong(REFRESH_TOKEN_EXPIRY, -1)

    private val isRefreshTokenValid: Boolean
        get() =
            refreshToken?.isNotEmpty() == true && (refreshTokenExpiry > System.currentTimeMillis() || refreshTokenExpiry == -1L)

    val isAccessTokenValid: Boolean
        get() = accessTokenExpiry > System.currentTimeMillis()

    private var accessToken: String? = crypto.get(CryptoKeys.ACCESS_TOKEN)
    var refreshToken: String? = crypto.get(CryptoKeys.REFRESH_TOKEN_KEY)
        private set
    var userIdToken: String = crypto.get(CryptoKeys.USER_ID_KEY) ?: ""
        private set

    private var kmliChecked: Boolean
        get() = keepMeLoggedIn
        set(value) {
            keepMeLoggedIn = value
        }

    init {
        kmliChecked = prefs.getKeepMeLoggedIn()
    }

    val isLoggedIn
        get() = if (kmliChecked && isRefreshTokenValid || !kmliChecked && isAccessTokenValid) {
            true
        } else {
            when {
                !isAccessTokenValid && !kmliChecked && isRefreshTokenValid ->
                    prefs.setLogoutReason(LogoutReason.TOKEN_EXPIRED_LOGOUT)
                !refreshToken.isNullOrBlank() ->
                    prefs.setLogoutReason(LogoutReason.NO_LOGOUT_REASON)
            }
            logOut()
            false
        }

    fun fetchAccessToken(): String {
        if (refreshToken.isNullOrBlank()) {
            logOut()
            throw InvalidRefreshTokenException(if (kmliChecked) LogoutReason.TECHNICAL_LOGOUT else LogoutReason.TOKEN_EXPIRED_LOGOUT)
        }

        if (!isAccessTokenValid || accessToken.isNullOrBlank()) {
            refreshTokens()
        }
        return "Bearer $accessToken"
    }

    fun updateCredentials(credentials: Credentials, save: Boolean = false) {
        accessToken = credentials.accessToken
        refreshToken = credentials.refreshToken
        saveTokensExpiration(credentials.expiresIn, credentials.refreshExpiresIn)
        kmliChecked = save
        prefs.saveKeepMeLoggedIn(kmliChecked)
        crypto.store(CryptoKeys.REFRESH_TOKEN_KEY to credentials.refreshToken)
        if (kmliChecked)
            crypto.store(CryptoKeys.ACCESS_TOKEN to credentials.accessToken)
    }

    fun updateUserId(userId: String) {
        userIdToken = userId
        crypto.store(CryptoKeys.USER_ID_KEY to userId)
    }

    fun requestNewToken(): String {
        if (refreshToken.isNullOrBlank()) {
            logOut()
            throw InvalidRefreshTokenException(if (kmliChecked) LogoutReason.TECHNICAL_LOGOUT else LogoutReason.TOKEN_EXPIRED_LOGOUT)
        }
        refreshTokens()
        return "Bearer $accessToken"
    }

    private fun refreshTokens(): Credentials {
        try {
            return tokenApi.getNewToken(RefreshTokenRequest(refreshToken!!))
                .map { it.content }
                .doOnSuccess {
                    updateCredentials(it, kmliChecked)
                }
                .blockingGet()
        } catch (t: Throwable) {
            if (t is HttpException) {
                if (
                    t.code() == HttpResponseCodes.FORBIDDEN ||
                    t.code() == HttpResponseCodes.NOT_AUTHORIZED ||
                    t.code() == HttpResponseCodes.INVALID_CREDENTIALS
                ) {
                    logOut()
                    throw InvalidRefreshTokenException(if (kmliChecked) LogoutReason.TECHNICAL_LOGOUT else LogoutReason.TOKEN_EXPIRED_LOGOUT)
                }
            }
            throw t
        }
    }

    fun logOut() {
        accessToken = ""
        refreshToken = ""
        userIdToken = ""
        clearCaches()
    }

    private fun clearCaches() {
        crypto.remove(listOf(CryptoKeys.REFRESH_TOKEN_KEY, CryptoKeys.USER_ID_KEY, CryptoKeys.ACCESS_TOKEN))
        prefs.removePref(listOf(ACCESS_TOKEN_EXPIRY, REFRESH_TOKEN_EXPIRY))
    }

    private fun saveTokensExpiration(accessTokenLifeSpan: Int, refreshTokenLifespan: Int) {
        val currTime = System.currentTimeMillis()
        val accessTExpiry = currTime + TimeUnit.SECONDS.toMillis(accessTokenLifeSpan.toLong())
        val refreshTExpiry = currTime + TimeUnit.SECONDS.toMillis(refreshTokenLifespan.toLong())

        accessTokenExpiry = accessTExpiry

        prefs.sharedPreferences.edit().apply {
            putLong(ACCESS_TOKEN_EXPIRY, accessTExpiry)
            putLong(REFRESH_TOKEN_EXPIRY, refreshTExpiry)
            apply()
        }
    }
}
