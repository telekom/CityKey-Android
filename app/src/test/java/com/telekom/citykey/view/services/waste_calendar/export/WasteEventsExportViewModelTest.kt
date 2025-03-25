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

package com.telekom.citykey.view.services.waste_calendar.export

import androidx.lifecycle.Observer
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.networkinterface.models.waste_calendar.CalendarAccount
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.reactivex.Single
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import timber.log.Timber

@ExtendWith(MockKExtension::class)
@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class WasteEventsExportViewModelTest {

    private lateinit var viewModel: WasteEventsExportViewModel
    private val wasteCalendarInteractor: WasteCalendarInteractor = mockk()
    private val calendarAccount: CalendarAccount = mockk()
    private val observerEventsExported: Observer<Int> = mockk(relaxed = true)
    private val observerError: Observer<Unit> = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { wasteCalendarInteractor.filterCategories } returns emptyList()
        viewModel = WasteEventsExportViewModel(wasteCalendarInteractor)
        viewModel.eventsExported.observeForever(observerEventsExported)
        viewModel.error.observeForever(observerError)
    }

    @AfterEach
    fun tearDown() {
        viewModel.eventsExported.removeObserver(observerEventsExported)
        viewModel.error.removeObserver(observerError)
    }

    @Test
    fun `onAddClicked should update eventsExported on success`() {
        every { wasteCalendarInteractor.exportCalendarEvents(calendarAccount) } returns Single.just(3)
        viewModel.onAddClicked(calendarAccount)
        verify { observerEventsExported.onChanged(any()) }
    }

    @Test
    fun `onAddClicked should trigger error on failure`() {
        val exception = RuntimeException("Export failed")
        every { wasteCalendarInteractor.exportCalendarEvents(calendarAccount) } returns Single.error(exception)
        mockkObject(Timber)
        every { Timber.e(exception) } just Runs
        viewModel.onAddClicked(calendarAccount)
        verify { observerError.onChanged(Unit) }
        verify { Timber.e(exception) }
        unmockkObject(Timber)
    }
}
