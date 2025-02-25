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

package com.telekom.citykey.domain.legaldata

import com.google.gson.Gson
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.legal_data.LegalDataManager
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.models.content.Terms
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class LegalDataManagerTest {

    private val oscaRepository: OscaRepository = mockk(relaxed = true)
    private val preferences: PreferencesHelper = mockk(relaxed = true)
    private lateinit var legalDataManager: LegalDataManager

    @BeforeEach
    fun setUp() {
        legalDataManager = LegalDataManager(oscaRepository, preferences)
    }

    @Test
    fun `Data is missing so we need to get it - success`() {
        every { preferences.legalData } returns ""
        every { oscaRepository.getLegalData() } returns Maybe.just(mockk())

        legalDataManager.loadLegalData()
            .test()
            .assertNoErrors()
            .dispose()
    }

    private class ErrorException : Exception()

    @Test
    fun `Data is missing so we need to get it - error`() {
        every { preferences.legalData } returns ""
        every { oscaRepository.getLegalData() } returns Maybe.error(ErrorException())

        legalDataManager.loadLegalData()
            .test()
            .assertError(ErrorException::class.java)
            .dispose()
    }

    @Test
    fun `Data is there so we try to update`() {
        every { preferences.legalData } returns Gson().toJson(mockk<Terms>())
        every { oscaRepository.getLegalData() } returns Maybe.error(ErrorException())

        legalDataManager.loadLegalData()
            .test()
            .assertNoErrors()
            .assertOf { verify { oscaRepository.getLegalData() } }
            .dispose()
    }
}
