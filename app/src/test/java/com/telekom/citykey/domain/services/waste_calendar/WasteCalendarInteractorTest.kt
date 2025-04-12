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

package com.telekom.citykey.domain.services.waste_calendar

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.content.City
import com.telekom.citykey.networkinterface.models.waste_calendar.CalendarAccount
import com.telekom.citykey.networkinterface.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarAddress
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarPickups
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarReminder
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteItems
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Calendar
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class WasteCalendarInteractorTest {

    @MockK
    private lateinit var servicesRepository: ServicesRepository

    @MockK
    private lateinit var globalData: GlobalData

    @RelaxedMockK
    private lateinit var globalMessages: GlobalMessages

    @MockK
    private lateinit var wasteExportInteractor: WasteExportInteractor

    private lateinit var wasteCalendarInteractor: WasteCalendarInteractor

    // Test data
    private val testStreetName = "Test Street"
    private val testHouseNumber = "123"
    private val testCityId = 8
    private val testWasteTypes = listOf(
        GetWasteTypeResponse(1, "Paper"),
        GetWasteTypeResponse(2, "Plastic"),
        GetWasteTypeResponse(3, "Glass")
    )
    private val testWasteCalendarResponse = WasteCalendarResponse(
        address = WasteCalendarAddress(testStreetName, testHouseNumber),
        calendar = listOf(
            WasteCalendarPickups(
                Date(),
                listOf(
                    WasteItems.WasteItem("Paper", "", 1),
                    WasteItems.WasteItem("Plastic", "", 2),
                    WasteItems.WasteItem("Glass", "", 3)
                )
            )
        ),
        reminders = listOf(
            WasteCalendarReminder(
                wasteTypeId = 1,
                remindTime = "07:00",
                sameDay = true,
                oneDayBefore = false,
                twoDaysBefore = false
            )
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        // Mock GlobalData
        every { globalData.currentCityId } returns testCityId
        every { globalData.city } returns BehaviorSubject.createDefault(City(testCityId))
        every { globalData.user } returns BehaviorSubject.createDefault(UserState.Absent)
        every { globalData.cityColor } returns 0XFF0000

        // Setup interactor
        wasteCalendarInteractor = WasteCalendarInteractor(
            servicesRepository,
            globalData,
            globalMessages,
            wasteExportInteractor
        )
    }

    @Nested
    @DisplayName("Tests for filter methods")
    inner class FilterTests {

        @Test
        fun `setFilters should update filters list`() {
            // Arrange
            val filters = listOf("1", "2")

            // Act
            wasteCalendarInteractor.setFilters(filters)

            // Assert
            assertEquals(filters, wasteCalendarInteractor.filters)
        }

        @Test
        fun `appliyFilter should update filters and post count`() {
            // Arrange
            val filters = arrayListOf("1", "2")
            every { servicesRepository.getWasteCalendarFilterOptions(testCityId) } returns Maybe.just(testWasteTypes)
            wasteCalendarInteractor.setCategoriesCount(testWasteTypes)

            // Act
            wasteCalendarInteractor.appliyFilter(filters)

            // Assert
            assertEquals(filters, wasteCalendarInteractor.filters)
        }

        @Test
        fun `getFilterOptions should fetch options if empty`() {
            // Arrange
            every { servicesRepository.getWasteCalendarFilterOptions(testCityId) } returns Maybe.just(testWasteTypes)

            // Act
            val testObserver = wasteCalendarInteractor.getFilterOptions().test()

            // Assert
            testObserver.assertComplete()
            verify { servicesRepository.getWasteCalendarFilterOptions(testCityId) }
        }
    }

    @Nested
    @DisplayName("Tests for reminder functions")
    inner class ReminderTests {

        @Test
        fun `findReminder should return existing reminder`() {
            // Arrange
            every {
                servicesRepository.getWasteCalendar(any(), any())
            } returns Maybe.just(testWasteCalendarResponse)
            wasteCalendarInteractor.getData(testStreetName, testHouseNumber).test().assertComplete()

            // Act
            val testObserver = wasteCalendarInteractor.findReminder(1).test()

            // Assert
            testObserver.assertComplete()
            testObserver.assertValue { reminder ->
                reminder.wasteTypeId == 1 && reminder.remindTime == "07:00"
            }
        }

        @Test
        fun `saveReminder should call repository`() {
            // Arrange
            val reminder = WasteCalendarReminder(
                wasteTypeId = 1,
                remindTime = "08:00",
                sameDay = true,
                oneDayBefore = false,
                twoDaysBefore = false
            )
            every { servicesRepository.saveWasteCalendarReminder(reminder, testCityId) } returns Completable.complete()

            // Act
            wasteCalendarInteractor.saveReminder(reminder)

            // Assert
            verify { servicesRepository.saveWasteCalendarReminder(reminder, testCityId) }
        }
    }

    @Nested
    @DisplayName("Tests for monthly data")
    inner class MonthlyDataTests {

        @BeforeEach
        fun setupMonthlyData() {
            every {
                servicesRepository.getWasteCalendar(any(), any())
            } returns Maybe.just(testWasteCalendarResponse)
            wasteCalendarInteractor.getData(testStreetName, testHouseNumber).test().assertComplete()
        }

        @Test
        fun `getDataForSelectedMonth should emit data`() {
            // Arrange
            val calendar = Calendar.getInstance()
            val testObserver = wasteCalendarInteractor.monthlyDataSubject.test()

            // Act
            wasteCalendarInteractor.getDataForSelectedMonth(calendar)

            // Assert
            testObserver.assertValueCount(1)
        }
    }

    @Test
    fun `fetchAddressWaste should call repository`() {
        // Arrange
        val streetName = "Test Street"
        every {
            servicesRepository.getWasteAddressDetails(streetName, testCityId)
        } returns Observable.just(WasteAddressState.Success(emptyList()))

        // Act
        val testObserver = wasteCalendarInteractor.ftuAddressSubject.test()
        print(WasteAddressState.Error(RuntimeException()))
        wasteCalendarInteractor.fetchAddressWaste(streetName)

        // Skip the debounce by advancing time
        testObserver.awaitCount(1)

        // Assert
        verify { servicesRepository.getWasteAddressDetails(streetName, testCityId) }
    }

    @Test
    fun `exportCalendarEvents should call wasteExportInteractor`() {
        // Arrange
        val account = CalendarAccount(1L, 0xFF2345, "", "")
        every { wasteExportInteractor.exportCalendarEvents(any(), account, any()) } returns Single.just(5)

        // Act
        val testObserver = wasteCalendarInteractor.exportCalendarEvents(account).test()

        // Assert
        testObserver.assertComplete()
        verify { wasteExportInteractor.exportCalendarEvents(any(), account, any()) }
    }

    @Test
    fun `getCalendarsInfo should call wasteExportInteractor`() {
        // Arrange
        every { wasteExportInteractor.getCalendarsInfo() } returns Single.just(emptyList())

        // Act
        val testObserver = wasteCalendarInteractor.getCalendarsInfo().test()

        // Assert
        testObserver.assertComplete()
        verify { wasteExportInteractor.getCalendarsInfo() }
    }
}
