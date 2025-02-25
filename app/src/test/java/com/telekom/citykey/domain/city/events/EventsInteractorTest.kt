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

package com.telekom.citykey.domain.city.events

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.City
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class EventsInteractorTest {

    private val repository: CityRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private val globalMessages: GlobalMessages = mockk()
    private lateinit var eventsInteractor: EventsInteractor

    private val userSubject: BehaviorSubject<UserState> = BehaviorSubject.create()
    private val citySubject: BehaviorSubject<City> = BehaviorSubject.create()

    @BeforeEach
    fun setUp() {
        every { globalData.user } returns userSubject
        every { globalData.city } returns citySubject
        eventsInteractor = EventsInteractor(repository, globalMessages, globalData)
    }

    @Test
    fun `Test if events would refresh on city change`() {
        val newCityId = 1
        every { globalData.currentCityId } returns newCityId
        every { globalData.isUserLoggedIn } returns true

        citySubject.onNext(
            mockk {
                every { cityName } returns "City"
                every { cityId } returns newCityId
                every { cityConfig?.eventsCount } returns 4
                every { cityConfig?.yourEventsCount } returns 1
            }
        )

        verify(exactly = 1) { repository.getEvents(newCityId, pageNo = 1) }
    }

    @Test
    fun `Test if favored events would refresh on city change`() {
        every { globalData.currentCityId } returns 1
        every { globalData.isUserLoggedIn } returns true

        citySubject.onNext(
            mockk {
                every { cityName } returns "City"
                every { cityId } returns 1
                every { cityConfig?.eventsCount } returns 4
                every { cityConfig?.yourEventsCount } returns 1
            }
        )

        verify(exactly = 1) { repository.getFavoredEvents(1) }
    }

    @Test
    fun `Test if favored event would refresh on user log in`() {
        every { globalData.isUserLoggedIn } returns true
        userSubject.onNext(UserState.Present(mockk()))

        verify(exactly = 1) { repository.getFavoredEvents(any()) }
    }

    @Test
    fun `Test favored events updating on user floating state`() {
        every { globalData.isUserLoggedIn } returns true
        userSubject.onNext(UserState.Present(mockk()))
        every { globalData.isUserLoggedIn } returns false
        userSubject.onNext(UserState.Absent)
        every { globalData.isUserLoggedIn } returns true
        userSubject.onNext(UserState.Present(mockk()))
        verify(exactly = 2) { repository.getFavoredEvents(any()) }
    }

    @Test
    fun `Test favored events updating on user floating state ending with logged out`() {
        every { globalData.isUserLoggedIn } returns true
        userSubject.onNext(UserState.Present(mockk()))
        every { globalData.isUserLoggedIn } returns false
        userSubject.onNext(UserState.Absent)
        verify(exactly = 1) { repository.getFavoredEvents(any()) }
        assert(eventsInteractor.favoredEvents.blockingFirst().isEmpty())
    }

    @Test
    fun `Test dispatching favored events on user update`() {
        every { repository.getFavoredEvents(any()) } returns Maybe.just(listOf(mockk(), mockk(), mockk()))

        every { globalData.isUserLoggedIn } returns true

        userSubject.onNext(UserState.Present(mockk()))

        verify(exactly = 1) { repository.getFavoredEvents(any()) }
        assert(eventsInteractor.favoredEvents.blockingFirst().isNotEmpty())
        assert(eventsInteractor.favoredEvents.blockingFirst().size == 3)
    }
}
