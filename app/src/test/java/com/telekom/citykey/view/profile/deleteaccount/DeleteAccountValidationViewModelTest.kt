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

package com.telekom.citykey.view.profile.deleteaccount

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.auth.OAuth2TokenManager
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.networkinterface.models.error.OscaError
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.view.user.profile.delete_account.DeleteAccountValidationViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class DeleteAccountValidationViewModelTest {

    private val repository: UserRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private lateinit var viewModel: DeleteAccountValidationViewModel

    @BeforeEach
    fun setUp() {
        viewModel = DeleteAccountValidationViewModel(repository, globalData)
        OAuth2TokenManager.keepMeLoggedIn = true
    }

    @Test
    fun onConfirmClicked_invalidPassword() {
        viewModel.onConfirmClicked("kfkkq.")

        verify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun onConfirmClicked_invalid_password_online() {
        every { repository.deleteUser(any()) } returns generateError(ErrorCodes.ACCOUNT_WRONG_PASSWORD)

        viewModel.onConfirmClicked("kakdkaksd.")

        assert(viewModel.error.value != null)
        verify(exactly = 1) { repository.deleteUser(any()) }
    }

    @Test
    fun onConfirmClicked_no_connection() {
        every { repository.deleteUser(any()) } returns Completable.error(mockk<NoConnectionException>())

        viewModel.onConfirmClicked("kakdkaksd.")

        assert(viewModel.error.value == null)
        assert(viewModel.showRetryDialog.value != null)
        verify(exactly = 1) { repository.deleteUser(any()) }
    }

    private fun generateError(errorCode: String): Completable {
        return Completable.error(
            NetworkException(
                0,
                OscaErrorResponse(
                    listOf(
                        OscaError(
                            "test",
                            errorCode
                        )
                    )
                ),
                "",
                Exception()
            )
        )
    }
}
