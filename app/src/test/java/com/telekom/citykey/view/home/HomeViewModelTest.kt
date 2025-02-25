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

package com.telekom.citykey.view.home

import android.graphics.Color
import androidx.lifecycle.Observer
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.weather.WeatherInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.CityConfig
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.models.live_data.HomeData
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel
    private val globalData: GlobalData = mockk(relaxed = true)
    private val eventsInteractor: EventsInteractor = mockk(relaxed = true)
    private val newsInteractor: NewsInteractor = mockk(relaxed = true)
    private val weatherInteractor: WeatherInteractor = mockk(relaxed = true)
    private val preferencesHelper: PreferencesHelper = mockk(relaxed = true)
    private val cityConfigMock: CityConfig = mockk(relaxed = true)
    private val userProfile: UserProfile = mockk(relaxed = true)

    @Test
    fun `Test init city details, city events empty list, city selection tooltip`() {
        val viewTypes = mutableListOf<Int>()
        viewTypes.add(HomeViewTypes.VIEW_TYPE_NEWS)
        viewTypes.add(HomeViewTypes.VIEW_TYPE_EVENTS)
        val homeData = HomeData(
            "city", "coat",
            Color.parseColor("color"),
            viewTypes
        )
        val userState = UserState.Present(userProfile)

        every { globalData.city } returns Observable.just(
            mockk(relaxed = true) {
                every { cityName } returns "city"
                every { cityColor } returns "color"
                every { municipalCoat } returns "coat"
                every { cityPicture } returns "path"
                every { cityNightPicture } returns "cityNightPicturePath"
                every { cityConfig } returns cityConfigMock
            }
        )
        every { globalData.user } returns Observable.just(userState)
        every { eventsInteractor.favoredEvents } returns Observable.just(emptyList())
        every { preferencesHelper.getShowedCitySelectionToolTip() } returns true

        var response: Boolean? = null
        val observer = Observer<Boolean> { response = it }

        // observeCityDetails
        homeViewModel = HomeViewModel(
            globalData, eventsInteractor, newsInteractor,
            weatherInteractor, preferencesHelper
        )

        homeViewModel.citySelectionTooltipState.observeForever(observer)
        assertEquals(homeData, homeViewModel.homeData.value)

        // Test User State
        assertEquals(true, homeViewModel.userState.value)

        // Show tooltip
        assertEquals(false, response)

        homeViewModel.citySelectionTooltipState.removeObserver(observer)
    }

    @Test
    fun `Test onCitySelectionClicked`() {
        val userState = UserState.Present(userProfile)

        every { globalData.city } returns Observable.just(
            mockk(relaxed = true) {
                every { cityName } returns "city"
                every { cityColor } returns "color"
                every { municipalCoat } returns "coat"
                every { cityPicture } returns "path"
                every { cityConfig } returns cityConfigMock
            }
        )
        every { globalData.user } returns Observable.just(userState)
        every { eventsInteractor.favoredEvents } returns Observable.just(emptyList())
        every { preferencesHelper.getShowedCitySelectionToolTip() } returns true

        homeViewModel = HomeViewModel(
            globalData, eventsInteractor, newsInteractor,
            weatherInteractor, preferencesHelper
        )

        var response: Boolean? = null
        val observer = Observer<Boolean> { response = it }

        homeViewModel.citySelectionTooltipState.observeForever(observer)
        homeViewModel.onTooltipDismissed()
        verify { preferencesHelper.saveShowedCitySelectionToolTip() }
        assertEquals(false, response)
        homeViewModel.citySelectionTooltipState.removeObserver(observer)
    }

    @Test
    fun `Test onRefresh`() {
        every { globalData.refreshContent() } returns Completable.complete()
        homeViewModel = HomeViewModel(
            globalData, eventsInteractor, newsInteractor,
            weatherInteractor, preferencesHelper
        )
        homeViewModel.onRefresh()
        assertEquals(Unit, homeViewModel.refreshFinished.value)
    }
}
