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

package com.telekom.citykey.view.services.waste_calendar

import androidx.lifecycle.Observer
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.networkinterface.models.waste_calendar.CalendarAccount
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Calendar

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class WasteCalendarViewModelTest {

    @MockK
    private lateinit var wasteCalendarInteractor: WasteCalendarInteractor

    @MockK
    private lateinit var globalData: GlobalData

    @RelaxedMockK
    private lateinit var filtersObserver: Observer<Pair<Int, Int>>

    @RelaxedMockK
    private lateinit var addressObserver: Observer<String>

    @RelaxedMockK
    private lateinit var calendarAccountsObserver: Observer<List<CalendarAccount>>

    private lateinit var viewModel: WasteCalendarViewModel

    @BeforeEach
    fun setup() {

        MockKAnnotations.init(this)

        // Setup for wasteCalendarInteractor
        every { wasteCalendarInteractor.filters.size } returns 2
        every { wasteCalendarInteractor.categoriesSize } returns 5
        every { wasteCalendarInteractor.fullAddress } returns "Sample Address"
        every { wasteCalendarInteractor.monthlyDataSubject } returns mockk(relaxed = true)
        every { wasteCalendarInteractor.updateSelectedWasteCount } returns mockk(relaxed = true)
        every { wasteCalendarInteractor.getDataForSelectedMonth(any()) } just runs
        every { wasteCalendarInteractor.getCalendarsInfo() } returns Single.just(listOf(mockk<CalendarAccount>(relaxed = true)))

        // Setup for globalData
        every { globalData.user } returns Observable.never()

        // Initialize the ViewModel
        viewModel = WasteCalendarViewModel(wasteCalendarInteractor, globalData)

        // Observe LiveData objects
        viewModel.appliedFilters.observeForever(filtersObserver)
        viewModel.appliedAddress.observeForever(addressObserver)
        viewModel.calendarAccounts.observeForever(calendarAccountsObserver)
    }

    @Test
    fun `onViewCreated should update filters and address`() {
        // When
        viewModel.onViewCreated()

        // Then
        verify { filtersObserver.onChanged(2 to 5) }
        verify { addressObserver.onChanged("Sample Address") }
    }

    @Test
    fun `onMonthChanged should call getDataForSelectedMonth with calendar`() {
        // Given
        val calendar = Calendar.getInstance()

        // When
        viewModel.onMonthChanged(calendar)

        // Then
        verify { wasteCalendarInteractor.getDataForSelectedMonth(calendar) }
    }

    @Test
    fun `onStreetUpdated should update address LiveData`() {
        // When
        viewModel.onStreetUpdated()

        // Then
        verify { addressObserver.onChanged("Sample Address") }
    }

    @Test
    fun `onExportBtnClicked should fetch calendar info and post to LiveData`() {
        // Given
        val calendarAccounts = listOf(mockk<CalendarAccount>(relaxed = true))
        every { wasteCalendarInteractor.getCalendarsInfo() } returns Single.just(calendarAccounts)

        // When
        viewModel.onExportBtnClicked()

        // Then
        verify { wasteCalendarInteractor.getCalendarsInfo() }
        verify(timeout = 300) { calendarAccountsObserver.onChanged(calendarAccounts) }
    }

    @Test
    fun `onCategoriesUpdated should update filter counts`() {
        // When
        viewModel.onCategoriesUpdated()

        // Then
        print(viewModel.wasteData)
        print(viewModel.userLoggedOut)
        verify { filtersObserver.onChanged(2 to 5) }
    }
}
