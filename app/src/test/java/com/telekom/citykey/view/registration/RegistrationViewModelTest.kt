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

package com.telekom.citykey.view.registration

import androidx.core.util.PatternsCompat
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.R
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.view.user.registration.RegFields
import com.telekom.citykey.view.user.registration.RegistrationViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class RegistrationViewModelTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private lateinit var registrationViewModel: RegistrationViewModel
    private val hints = arrayOf(
        "minimum 8 characters",
        "at least one symbol",
        "at least one digit"
    )
    private val formattedHints: String =
        "Please use: minimum 8 characters, at least one symbol, at least one digit"

    @BeforeEach
    fun setUp() {
        registrationViewModel =
            RegistrationViewModel(userRepository)
        registrationViewModel.onPasswordHintsResourcesReceived(hints, formattedHints)
    }

    @Test
    fun `Test validate password if length less than 7 `() {
        registrationViewModel.onPasswordTextChanged("pass")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_too_weak
            )
        )
    }

    @Test
    fun `Test validate password if length greater than 7 `() {
        registrationViewModel.onPasswordTextChanged("password")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_too_weak
            )
        )
    }

    @Test
    fun `Test validate password if length greater than 7 contains Special Symbols `() {
        registrationViewModel.onPasswordTextChanged("password@")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_too_weak
            )
        )
    }

    @Test
    fun `Test validate password if password and email are same `() {
        registrationViewModel.onEmailUpdated("password@0", "password@0")
        registrationViewModel.onPasswordTextChanged("password@0")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_email_same
            )
        )
    }

    @Test
    fun `Test validate password if length greater than 7 contains Special Symbols and digits`() {
        registrationViewModel.onPasswordTextChanged("password@0")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(FieldValidation.SUCCESS, "")
        )
    }

    @Test
    fun `Test on post code ready if empty`() {
        registrationViewModel.onPostCodeReady("")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.POSTAL_CODE to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_empty_field
            )
        )
    }

    @Test
    fun `Test on post code ready if not empty & length less than 5`() {
        registrationViewModel.onPostCodeReady("1111")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.POSTAL_CODE to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_postcode
            )
        )
    }

    @Test
    fun `Test on email ready if empty`() {
        registrationViewModel.onEmailReady("")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_empty_field
            )
        )
    }

    @Disabled
    @Test
    fun `Test on email ready if non-empty and invalid email`() {
        mockkObject(PatternsCompat.EMAIL_ADDRESS)
        every { PatternsCompat.EMAIL_ADDRESS.matcher(any()).matches() } returns false

        registrationViewModel.onEmailReady("abc@")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_email
            )
        )
    }

    @Test
    fun `Test validate second password if second password empty`() {
        registrationViewModel.onSecondPasswordTextChanged("password@0" to "")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.SEC_PASSWORD to
                    FieldValidation(
                        FieldValidation.ERROR, null,
                        R.string.r_001_registration_error_empty_field
                    )
        )
    }

    @Test
    fun `Test validate second password if non-empty and matches first and second password`() {
        registrationViewModel.onSecondPasswordTextChanged("password@0" to "password@0")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.SEC_PASSWORD to FieldValidation(FieldValidation.OK, null)
        )
    }
}
