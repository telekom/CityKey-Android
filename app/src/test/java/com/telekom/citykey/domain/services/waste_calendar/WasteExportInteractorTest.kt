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

import android.content.ContentResolver
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.networkinterface.models.waste_calendar.CalendarAccount
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarPickups
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteItems
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class WasteExportInteractorTest {

    private lateinit var contentResolver: ContentResolver
    private lateinit var wasteExportInteractor: WasteExportInteractor
    private lateinit var mockCursor: Cursor

    @BeforeEach
    fun setup() {
        // Setup RxJava schedulers for testing
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        contentResolver = mockk<ContentResolver>()
        mockCursor = mockk<MatrixCursor>()
        wasteExportInteractor = WasteExportInteractor(contentResolver)
    }

    @AfterEach
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
        clearAllMocks()
    }

    @Test
    @DisplayName("getCalendarsInfo should query ContentResolver when calendarInfo is empty")
    fun getCalendarsInfoWhenEmpty() {
        // Given
        val projections = arrayOf(
            Calendars._ID,
            Calendars.ACCOUNT_NAME,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.CALENDAR_COLOR
        )

        // Prepare cursor with sample data
        every {
            contentResolver.query(
                Calendars.CONTENT_URI,
                projections,
                null,
                null,
                null
            )
        } returns mockCursor

        every { mockCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockCursor.count } returns 2
        every { mockCursor.getLong(0) } returnsMany listOf(1L, 2L)
        every { mockCursor.getString(1) } returnsMany listOf("account1@example.com", "account2@example.com")
        every { mockCursor.getString(2) } returnsMany listOf("Calendar 1", "Calendar 2")
        every { mockCursor.getInt(3) } returnsMany listOf(123456, 654321)
        every { mockCursor.close() } just Runs

        // When
        val result = wasteExportInteractor.getCalendarsInfo().blockingGet()

        // Then
        verify(exactly = 1) {
            contentResolver.query(
                Calendars.CONTENT_URI,
                projections,
                null,
                null,
                null
            )
        }
        assertEquals(2, result.size)
        assertEquals(1L, result[0].calId)
        assertEquals("Calendar 1", result[0].calendarDisplayName)
    }

    @Test
    @DisplayName("getCalendarsInfo should return cached data when already loaded")
    fun getCalendarsInfoWhenCached() {
        // Given - load calendar info first
        val projections = arrayOf(
            Calendars._ID,
            Calendars.ACCOUNT_NAME,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.CALENDAR_COLOR
        )

        every {
            contentResolver.query(
                Calendars.CONTENT_URI,
                projections,
                null,
                null,
                null
            )
        } returns mockCursor

        every { mockCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockCursor.count } returns 2
        every { mockCursor.getLong(0) } returnsMany listOf(1L, 2L)
        every { mockCursor.getString(1) } returnsMany listOf("account1@example.com", "account2@example.com")
        every { mockCursor.getString(2) } returnsMany listOf("Calendar 1", "Calendar 2")
        every { mockCursor.getInt(3) } returnsMany listOf(123456, 654321)
        every { mockCursor.close() } just Runs

        // First call to populate cache
        wasteExportInteractor.getCalendarsInfo().blockingGet()

        // Clear mocks to verify no more calls
        clearMocks(contentResolver)

        // When - call again
        val result = wasteExportInteractor.getCalendarsInfo().blockingGet()

        // Then
        verify(exactly = 0) { contentResolver.query(any(), any(), any(), any(), any()) }
        assertEquals(2, result.size)
    }

    @Test
    @DisplayName("exportCalendarEvents should filter waste pickups and insert events")
    fun exportCalendarEvents() {
        // Given
        val wastePickups = WasteCalendarPickups(
            date = Date(),
            wasteTypeList = listOf(
                WasteItems.WasteItem("Paper", "", 1),
                WasteItems.WasteItem("Plastic", "", 2),
                WasteItems.WasteItem("Glass", "", 3)
            )
        )
        val calendarAccount = CalendarAccount(1L, 123456, "Test Calendar", "test@example.com")
        val filters = listOf("1", "2")

        // Mock bulk insert to return number of inserted items
        every {
            contentResolver.bulkInsert(Events.CONTENT_URI, any())
        } returns 2

        // When
        val result = wasteExportInteractor.exportCalendarEvents(
            mutableListOf(wastePickups),
            calendarAccount,
            filters
        ).blockingGet()

        // Then
        verify { contentResolver.bulkInsert(Events.CONTENT_URI, any()) }
        assertEquals(2, result)
    }
}
