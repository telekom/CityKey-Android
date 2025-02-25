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

package com.telekom.citykey.view.profile.changeresidence

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.R
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.user.ResidenceValidationResponse
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.profile.change_residence.ChangeResidenceViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class ChangeResidenceViewModelTest {


    private val repository: UserRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private val userInteractor: UserInteractor = mockk(relaxed = true)
    private lateinit var changeResidenceViewModel: ChangeResidenceViewModel

    @BeforeEach
    fun setUp() {
        changeResidenceViewModel = ChangeResidenceViewModel(
            globalData,
            repository, userInteractor
        )
    }

    @Test
    fun `Test onSaveClicked`() {
        val oscaResponse = mockk<ResidenceValidationResponse>(relaxed = true) {
            every { cityName } returns "Bonn"
            every { homeCityId } returns 0
        }
        every { repository.validatePostalCode("11111") } returns
                Maybe.just(oscaResponse)
        every {
            repository.changePersonalData(
                mockk(relaxed = true),
                "postalCode"
            )
        } returns Completable.complete()

        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(oscaResponse, changeResidenceViewModel.requestSent.value)
    }

    @Test
    fun `Test onSaveClicked on error throw invalidRefreshTokenException`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(InvalidRefreshTokenException(LogoutReason.TECHNICAL_LOGOUT))
        changeResidenceViewModel.onSaveClicked("11111")
        verify { globalData.logOutUser(LogoutReason.TECHNICAL_LOGOUT) }
        assertEquals(Unit, changeResidenceViewModel.logUserOut.value)
    }

    @Test
    fun `Test onSaveClicked on error throw NoConnectionException`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(mockk<NoConnectionException>(relaxed = true))
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals("Change_Postcode", changeResidenceViewModel.showRetryDialog.value)
    }

    @Test
    fun `Test onSaveClicked on error throw NetworkException multipe error`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(
                    mockk<NetworkException>(relaxed = true) {
                        every { error } returns OscaErrorResponse(
                            arrayListOf(
                                mockk(relaxed = true) {
                                    every { errorCode } returns "form.validation.error"
                                }
                            )
                        )
                    }
                )
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(
            R.string.p_003_profile_email_change_technical_error,
            changeResidenceViewModel.generalErrors.value
        )
    }

    @Test
    fun `Test onSaveClicked on error throw NetworkException postal code error`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(
                    mockk<NetworkException>(relaxed = true) {
                        every { error } returns OscaErrorResponse(
                            arrayListOf(
                                mockk(relaxed = true) {
                                    every { errorCode } returns "postalCode.validation.error"
                                    every { userMsg } returns "userMsg"
                                }
                            )
                        )
                    }
                )
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(Pair("userMsg", null), changeResidenceViewModel.onlineErrors.value)
    }

    @Test
    fun `Test onSaveClicked on error throw any other exception`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(mockk<NullPointerException>(relaxed = true))
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(
            Unit,
            changeResidenceViewModel.technicalError.value
        )
    }
}
