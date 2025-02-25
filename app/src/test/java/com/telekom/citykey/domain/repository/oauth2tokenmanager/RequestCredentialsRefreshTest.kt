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
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.domain.repository.oauth2tokenmanager

import com.telekom.citykey.domain.repository.HttpResponseCodes
import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.SmartCredentialsApi
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.domain.security.crypto.CryptoKeys
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.user.Credentials
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException


class RequestCredentialsRefreshTest {

    private val tokenApi: SmartCredentialsApi = mockk(relaxed = true)
    private val crypto: Crypto = mockk(relaxed = true)
    private lateinit var oAuth2TokenManager: OAuth2TokenManager
    private val prefs: PreferencesHelper = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        OAuth2TokenManager.keepMeLoggedIn = true
    }

    @Test
    fun `When there is no refresh token`() {
        every { crypto.get(CryptoKeys.REFRESH_TOKEN_KEY) } returns null

        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)
        assertNull(oAuth2TokenManager.refreshToken)

        assertThrows(InvalidRefreshTokenException::class.java) {
            oAuth2TokenManager.requestNewToken()
        }
    }

    @Test
    fun `When refresh token is invalid`() {
        every { crypto.get(CryptoKeys.REFRESH_TOKEN_KEY) } returns null
        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)

        assertThrows(InvalidRefreshTokenException::class.java) {
            oAuth2TokenManager.requestNewToken()
        }

        verify { oAuth2TokenManager.logOut() }
        verify { crypto.remove(any()) }
    }

    @Test
    fun `When api returns NOT_AUTHORIZED`() {
        every { crypto.get(CryptoKeys.REFRESH_TOKEN_KEY) } returns "abc"
        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)

        every { tokenApi.getNewToken(any()) } returns Single.error(
            mockk<HttpException>(relaxed = true) {
                every { code() } returns HttpResponseCodes.NOT_AUTHORIZED
            }
        )

        assertThrows(InvalidRefreshTokenException::class.java) {
            oAuth2TokenManager.requestNewToken()
        }

        verify { oAuth2TokenManager.logOut() }
    }

    @Test
    fun `When api call was unsuccessful`() {
        every { crypto.get(CryptoKeys.REFRESH_TOKEN_KEY) } returns "abc"
        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)

        every { tokenApi.getNewToken(any()) } returns Single.error(
            mockk<HttpException> {
                every { code() } returns HttpResponseCodes.NOT_AUTHORIZED
            }
        )
        var newToken: String? = null
        assertThrows(InvalidRefreshTokenException::class.java) {
            newToken = oAuth2TokenManager.requestNewToken()
        }

        verify { oAuth2TokenManager.logOut() }
        assertNull(newToken)
    }

    @Test
    fun `When a network error happened`() {
        every { crypto.get(any()) } returns "abc"
        every { tokenApi.getNewToken(any()) } returns Single.error(
            mockk<HttpException>(relaxed = true) {
                every { code() } returns HttpResponseCodes.NOT_AUTHORIZED
            }
        )

        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)

        oAuth2TokenManager.updateCredentials(Credentials("a", "b", 3, 3, 3, true))

        assertThrows(RuntimeException::class.java) {
            oAuth2TokenManager.requestNewToken()
        }

        verify(exactly = 1) { tokenApi.getNewToken(any()) }
        verify(exactly = 0) { oAuth2TokenManager.logOut() }
    }

    @Test
    fun `When api call was successful`() {
        every { crypto.get(CryptoKeys.REFRESH_TOKEN_KEY) } returns "abc"
        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)

        val accessTokenNew = "abc_new"

        every { tokenApi.getNewToken(any()) } returns Single.just(createOscaResponse(accessTokenNew))

        val accessTokenCur = oAuth2TokenManager.requestNewToken()
        assertEquals("Bearer $accessTokenNew", accessTokenCur)
    }

    private fun createOscaResponse(accessToken: String): OscaResponse<Credentials> {
        return OscaResponse(createCredentials(accessToken))
    }

    private fun createCredentials(accessToken: String): Credentials {
        return Credentials(
            accessToken, "",
            10, 20, 0, false
        )
    }
}
