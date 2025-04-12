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

package com.telekom.citykey.view.home.eventsdetails

import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.OscaLocationManager
import com.telekom.citykey.data.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.networkinterface.models.content.UserProfile
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.view.home.events_details.EventDetailsViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class EventDetailsViewModelTest {

    private lateinit var eventDetailsViewModel: EventDetailsViewModel
    private val locationManager: OscaLocationManager = mockk(relaxed = true)
    private val eventsInteractor: EventsInteractor = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private val preferencesHelper: PreferencesHelper = mockk(relaxed = true)
    private val userProfile: UserProfile = mockk(relaxed = true)
    private val event: Event = mockk(relaxed = true)
    private val adjustManager: AdjustManager = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        val userState = UserState.Present(userProfile)
        every { globalData.user } returns Observable.just(userState)
        eventDetailsViewModel = EventDetailsViewModel(
            locationManager,
            eventsInteractor, globalData, preferencesHelper, adjustManager
        )
        assertEquals(true, eventDetailsViewModel.userLoggedIn.value)
    }

    @Test
    fun `Test onViewCreated if lat, long are not null`() {
        val listOfEvent = arrayListOf(event)
        every { eventsInteractor.favoredEvents } returns Observable.just(listOfEvent)
        every { eventsInteractor.favoritesErrors } returns Observable.just(NoConnectionException())

        eventDetailsViewModel.onViewCreated(
            mockk(relaxed = true) {
                every { latitude } returns 1.0
                every { longitude } returns 1.0
            }
        )
        assertEquals(LatLng(1.0, 1.0), eventDetailsViewModel.latLng.value)
        assertEquals(true, eventDetailsViewModel.favored.value)
        assertEquals(Unit, eventDetailsViewModel.showFavoritesLoadError.value)
    }

    @Test
    fun `Test onViewCreated if localAddress not null or blank`() {
        val listOfEvent = arrayListOf(event)
        every { locationManager.getLatLngFromAddress(any()) } returns Single.just(LatLng(0.0, 0.0))
        every { eventsInteractor.favoredEvents } returns Observable.just(listOfEvent)
        every { eventsInteractor.favoritesErrors } returns Observable.just(
            InvalidRefreshTokenException(LogoutReason.TECHNICAL_LOGOUT)
        )

        // getLatLngFromAddress
        eventDetailsViewModel.onViewCreated(
            mockk(relaxed = true) {
                every { latitude } returns 0.0
                every { longitude } returns 0.0
                every { locationAddress } returns "Bonn"
            }
        )

        assertEquals(LatLng(0.0, 0.0), eventDetailsViewModel.latLng.value)
        assertEquals(true, eventDetailsViewModel.favored.value)

        // favoritesErrors
        verify { globalData.logOutUser(LogoutReason.TECHNICAL_LOGOUT) }
    }

    @Test
    fun `Test onViewCreated if lat zero`() {
        eventDetailsViewModel.onFavoriteClicked(true, event)
        assertEquals(null, eventDetailsViewModel.latLng.value)
    }

    @Test
    fun `Test onFavoriteClicked success`() {
        every { eventsInteractor.setEventFavored(true, event) } returns Completable.complete()
        eventDetailsViewModel.onFavoriteClicked(true, event)
        assertEquals(true, eventDetailsViewModel.favored.value)
    }

    @Test
    fun testOnError_InvalidRefreshTokenException() {
        val exception = InvalidRefreshTokenException(LogoutReason.ACTIVE_LOGOUT)
        coEvery { eventsInteractor.setEventFavored(true, event) } returns Completable.error(exception)

        eventDetailsViewModel.onFavoriteClicked(true, event)

        verify { eventsInteractor.setEventFavored(true, event) }
        verify { globalData.logOutUser(LogoutReason.ACTIVE_LOGOUT) }
        assertEquals(true, eventDetailsViewModel.promptLoginRequired.value)
    }

}
