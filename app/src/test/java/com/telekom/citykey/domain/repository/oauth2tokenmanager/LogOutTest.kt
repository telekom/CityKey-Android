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

import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.SmartCredentialsApi
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.models.user.Credentials
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException

class LogOutTest {

    private val tokenApi: SmartCredentialsApi = mockk(relaxed = true)
    private val crypto: Crypto = mockk(relaxed = true)
    private lateinit var oAuth2TokenManager: OAuth2TokenManager
    private val prefs: PreferencesHelper = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        // since null would or might lead to empty tokens even without logout
        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)
        oAuth2TokenManager.updateCredentials(createCredentials(), true)
    }

    @Test
    fun `When user should be logged out`() {
        every { tokenApi.getNewToken(any()) } returns Single.error(
            mockk<HttpException>(relaxed = true) {
                every { code() } returns 400
            }
        )
        assertThrows(InvalidRefreshTokenException::class.java) {
            oAuth2TokenManager.fetchAccessToken()
        }

        verify { crypto.remove(any()) }
    }

    private fun createCredentials(): Credentials {
        return Credentials(
            "", "def",
            10, 20, 0, false
        )
    }
}
