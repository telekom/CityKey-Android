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

package com.telekom.citykey.view.profile.changeemail

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.data.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.networkinterface.models.error.OscaError
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.networkinterface.models.api.requests.EmailChangeRequest
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.profile.change_email.ChangeEmailViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class ChangeEmailViewModelTest {

    private val repository: UserRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private lateinit var spyChangeEmailViewModel: ChangeEmailViewModel

    @BeforeEach
    fun setUp() {
        spyChangeEmailViewModel =
            spyk(ChangeEmailViewModel(globalData, repository), recordPrivateCalls = true)
    }

    @Test
    fun onSaveClicked_should_not_change_email() {
        val email = "abcd"
        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) {
            repository.changeEmail(EmailChangeRequest("abc"))
        }
    }

    @Test
    fun onSaveClicked_should_change_email() {
        val email = "abcd@yahoo.com"

        every { repository.changeEmail(any()) } returns Completable.complete()
        spyChangeEmailViewModel.onSaveClicked(email)

        assert(spyChangeEmailViewModel.requestSent.value!!)
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_InvalidRefreshTokenException() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(
            InvalidRefreshTokenException(LogoutReason.TECHNICAL_LOGOUT)
        )

        spyChangeEmailViewModel.onSaveClicked(email)
        verify { globalData.logOutUser(LogoutReason.TECHNICAL_LOGOUT) }
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_NetworkException() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(
            NetworkException(
                0,
                OscaErrorResponse(
                    listOf(
                        OscaError(
                            "message",
                            "ec"
                        )
                    )
                ),
                "",
                Exception()
            )
        )

        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(spyChangeEmailViewModel.technicalError.value == Unit)
    }

    @Test
    fun onConfirmClicked_no_connection() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(mockk<NoConnectionException>())

        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(spyChangeEmailViewModel.showRetryDialog.value != null)
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_exception() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(Throwable("Just an error"))

        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(spyChangeEmailViewModel.technicalError.value == Unit)
    }
}
