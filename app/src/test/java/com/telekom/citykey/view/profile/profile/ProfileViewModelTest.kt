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

package com.telekom.citykey.view.profile.profile

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.content.UserProfile
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.profile.profile.ProfileViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class ProfileViewModelTest {


    private lateinit var profileViewModel: ProfileViewModel
    private val globalData: GlobalData = mockk(relaxed = true)
    private val userInteractor: UserInteractor = mockk(relaxed = true)
    private val availableCitiesInteractor: AvailableCitiesInteractor = mockk(relaxed = true)
    private val userProfile: UserProfile = mockk(relaxed = true)

    @Test
    fun `Test observe Profile if userState Present`() {
        val userState = UserState.Present(userProfile)
        every { userInteractor.user } returns Observable.just(userState)
        profileViewModel = ProfileViewModel(globalData, userInteractor, availableCitiesInteractor)
        assertEquals(UserState.Present(userProfile).profile, profileViewModel.profileContent.value)
    }

    @Test
    fun `Test observe Profile if userState Absent`() {
        val userState = UserState.Absent
        every { userInteractor.user } returns Observable.just(userState)
        profileViewModel = ProfileViewModel(globalData, userInteractor, availableCitiesInteractor)
        assertEquals(null, profileViewModel.logOutUser.value)
    }

    @Test
    fun `Test logout button clicked`() {
        profileViewModel = ProfileViewModel(globalData, userInteractor, availableCitiesInteractor)
        profileViewModel.onLogoutBtnClicked()
        verify { userInteractor.logOutUser(LogoutReason.ACTIVE_LOGOUT) }
    }
}
