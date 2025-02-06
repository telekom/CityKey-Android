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

package com.telekom.citykey.view.login

import android.app.PendingIntent
import com.google.android.gms.common.api.ResolvableApiException
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.smartlock.CredentialsClientHandler
import com.telekom.citykey.domain.user.smartlock.ResolvableException
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.view.user.login.login.LoginViewModel
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
class LoginViewModelTest {

    private val globalData: GlobalData = mockk(relaxed = true)
    private val smartLockCredentials: CredentialsClientHandler = mockk(relaxed = true)
    private val preferencesHelper: PreferencesHelper = mockk(relaxed = true)
    private val userInteractor: UserInteractor = mockk(relaxed = true)
    private val availableCitiesInteractor: AvailableCitiesInteractor = mockk(relaxed = true)
    private lateinit var loginViewModel: LoginViewModel

    @BeforeEach
    fun setUp() {
        loginViewModel =
            LoginViewModel(
                globalData,
                userInteractor,
                availableCitiesInteractor,
                smartLockCredentials,
                preferencesHelper
            )
    }

    @Test
    fun onLoginBtnPressed_input_valid_and_saved() {
        every { smartLockCredentials.saveCredentials(any(), any()) } returns
                Completable.create { it.onComplete() }
        every { userInteractor.logUserIn(any(), any()) } returns
                Maybe.just(
                    mockk {
                        every { email } returns "test@telekom.de"
                        every { accountId } returns "1"
                        every { dpnAccepted } returns true
                    }
                )

        loginViewModel.onLoginBtnPressed(
            email = "test@telekom.de",
            password = "test12345!",
            stayLoggedIn = false,
            isFirstTime = false
        )

        verify(exactly = 1) { userInteractor.logUserIn(any(), any()) }
        verify(exactly = 1) { smartLockCredentials.saveCredentials(any(), any()) }

        assert(loginViewModel.error.value == null)
        assert(loginViewModel.login.value == true)
    }

    @Test
    fun onLoginBtnPressed_resolution_required() {
        every { smartLockCredentials.saveCredentials(any(), any()) } returns
                Completable.create {
                    it.onError(
                        ResolvableException(
                            mockk<ResolvableApiException> {
                                every { resolution } returns mockk()
                            },
                            21
                        )
                    )
                }
        every { userInteractor.logUserIn(any(), any()) } returns
                Maybe.just(
                    mockk {
                        every { email } returns "test@telekom.de"
                        every { accountId } returns "1"
                    }
                )

        loginViewModel.onLoginBtnPressed(
            email = "test@telekom.de",
            password = "test12345!",
            stayLoggedIn = false,
            isFirstTime = false
        )

        verify(exactly = 1) { smartLockCredentials.saveCredentials(any(), any()) }
        assert(loginViewModel.resolutionSave.value is PendingIntent)
    }

    @Test
    fun onLoginSuccess_status_OK() {
        every { userInteractor.logUserIn(any(), any()) } returns
                Maybe.just(
                    mockk {
                        every { email } returns "test@telekom.de"
                        every { accountId } returns "1"
                    }
                )

        loginViewModel.onLoginBtnPressed(
            email = "test@telekom.de",
            password = "test12345!",
            stayLoggedIn = false,
            isFirstTime = false
        )

        verify(exactly = 1) { smartLockCredentials.saveCredentials(any(), any()) }
    }

    @Test
    fun `Test onLoginBtnPressed on error wrong Password Online`() {
        every { userInteractor.logUserIn(any(), any()) } returns
                Maybe.error(
                    mockk<NetworkException>(relaxed = true) {
                        every { error } returns mockk<OscaErrorResponse>(relaxed = true) {
                            every { errors } returns arrayListOf(
                                mockk(relaxed = true) {
                                    every { errorCode } returns ErrorCodes.INVALID_CREDENTIALS
                                    every { userMsg } returns "userMsg"
                                }
                            )
                        }
                    }
                )

        loginViewModel.onLoginBtnPressed(
            email = "test@telekom.de",
            password = "test12345!",
            stayLoggedIn = false,
            isFirstTime = false
        )

        assert(loginViewModel.error.value != null)
        assertEquals(
            FieldValidation(FieldValidation.ERROR, "userMsg"),
            loginViewModel.error.value
        )
    }

    @Test
    fun `Test onLoginBtnPressed on error email not confirmed`() {
        every { userInteractor.logUserIn(any(), any()) } returns
                Maybe.error(
                    mockk<NetworkException>(relaxed = true) {
                        every { error } returns mockk<OscaErrorResponse>(relaxed = true) {
                            every { errors } returns arrayListOf(
                                mockk(relaxed = true) {
                                    every { errorCode } returns ErrorCodes.LOGIN_EMAIL_NOT_CONFIRMED
                                }
                            )
                        }
                    }
                )

        loginViewModel.onLoginBtnPressed(
            email = "test@telekom.de",
            password = "test12345!",
            stayLoggedIn = false,
            isFirstTime = false
        )

        assertEquals(null, loginViewModel.emailNotConfirmed.value)
    }

    @Test
    fun `Test onLoginBtnPressed on error No Connection`() {
        every { userInteractor.logUserIn(any(), any()) } returns
                Maybe.error(mockk<NoConnectionException>(relaxed = true))

        loginViewModel.onLoginBtnPressed(
            email = "test@telekom.de",
            password = "test12345!",
            stayLoggedIn = false,
            isFirstTime = false
        )

        assertEquals(null, loginViewModel.showRetryDialog.value)
    }
}
