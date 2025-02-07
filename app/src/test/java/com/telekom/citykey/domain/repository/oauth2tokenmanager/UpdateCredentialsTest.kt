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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.domain.repository.oauth2tokenmanager

import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.SmartCredentialsApi
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.domain.security.crypto.CryptoKeys
import com.telekom.citykey.models.user.Credentials
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class UpdateCredentialsTest {

    private val tokenApi: SmartCredentialsApi = mockk(relaxed = true)
    private val crypto: Crypto = mockk(relaxed = true)
    private lateinit var oAuth2TokenManager: OAuth2TokenManager
    private val prefs: PreferencesHelper = mockk(relaxed = true)

    companion object {
        const val TOKEN_START = "Bearer "
    }

    @BeforeEach
    fun setUp() {
        every { crypto.get(CryptoKeys.REFRESH_TOKEN_KEY) } returns "abc"

        oAuth2TokenManager = spyk(OAuth2TokenManager(tokenApi, crypto, prefs))
        every { oAuth2TokenManager.isAccessTokenValid } returns true
    }

    @Test
    fun `When credentials sould not be stored`() {
        oAuth2TokenManager.updateCredentials(createEndCredentials(), false)

        verify(exactly = 1) { crypto.store(any()) }
        verify(exactly = 0) { crypto.remove(any()) }

        val endToken = "$TOKEN_START${createEndCredentials().accessToken}"
        assertEquals(endToken, oAuth2TokenManager.fetchAccessToken())
    }

    @Test
    fun `When credentials should be stored`() {
        oAuth2TokenManager.updateCredentials(createEndCredentials(), true)

        verify(exactly = 2) { crypto.store(any()) }
        verify(exactly = 0) { crypto.remove(any()) }

        val endToken = "$TOKEN_START${createEndCredentials().accessToken}"
        assertEquals(endToken, oAuth2TokenManager.fetchAccessToken())
    }

    private fun createCredentials(prefix: String, save: Boolean): Credentials {
        return Credentials(
            "${prefix}_accessToken", "${prefix}_refreshToken",
            10, 20, 0, save
        )
    }

    private fun createEndCredentials(save: Boolean = false): Credentials {
        return createCredentials("end", save)
    }
}
