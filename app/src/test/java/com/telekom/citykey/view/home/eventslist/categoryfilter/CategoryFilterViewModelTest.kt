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

package com.telekom.citykey.view.home.eventslist.categoryfilter

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.City
import com.telekom.citykey.models.content.EventCategory
import com.telekom.citykey.view.home.events_list.category_filter.CategoryFilterViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class CategoryFilterViewModelTest {

    private val city: City = mockk(relaxed = true)
    private val cityRepository: CityRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private val eventsInteractor: EventsInteractor = mockk(relaxed = true)
    private lateinit var spyCategoryFilterViewModel: CategoryFilterViewModel
    private val eventCategories = listOf(
        EventCategory(1, "Lesen"),
        EventCategory(2, "Sport"),
        EventCategory(3, "Natur")
    )

    @BeforeEach
    fun setUp() {
        val cityColor = "#2FADED"
        every { city.cityColor } returns cityColor
        every { globalData.city } returns BehaviorSubject.createDefault(city)

        every { cityRepository.getAllEventCategories(any()) } returns
                Maybe.just(eventCategories)

        spyCategoryFilterViewModel = spyk(
            CategoryFilterViewModel(globalData, cityRepository, eventsInteractor),
            recordPrivateCalls = true
        )
    }

    @Test
    fun init_should_succeed() {
        assert(spyCategoryFilterViewModel.allCategories.value == eventCategories)
        assert(spyCategoryFilterViewModel.filters.value != null)
    }

    @Test
    fun revokeFiltering() {
        spyCategoryFilterViewModel.revokeFiltering()
        verify { eventsInteractor.revokeEventsCount() }
    }

    @Test
    fun confirmFiltering() {
        spyCategoryFilterViewModel.confirmFiltering()

        verify { eventsInteractor.updateCategories(arrayListOf(), arrayListOf()) }
        verify { eventsInteractor.applyFilters() }
    }
}
