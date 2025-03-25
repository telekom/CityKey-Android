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

package com.telekom.citykey.view.services.waste_calendar.reminders

import androidx.lifecycle.Observer
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.notifications.NotificationsStateController
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarReminder
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class WasteReminderViewModelTest {

    private lateinit var viewModel: WasteReminderViewModel
    private val globalData: GlobalData = mockk()
    private val wasteCalendarInteractor: WasteCalendarInteractor = mockk()
    private val notificationsStateController: NotificationsStateController = mockk(relaxed = true)
    private val adjustManager: AdjustManager = mockk(relaxed = true)
    private val observerReminder: Observer<WasteCalendarReminder> = mockk(relaxed = true)
    private val observerCityColor: Observer<Int> = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { globalData.cityColor } returns 0xFFFFFF
        viewModel =
            WasteReminderViewModel(globalData, wasteCalendarInteractor, notificationsStateController, adjustManager)
        viewModel.reminder.observeForever(observerReminder)
        viewModel.cityColor.observeForever(observerCityColor)
    }

    @AfterEach
    fun tearDown() {
        viewModel.reminder.removeObserver(observerReminder)
        viewModel.cityColor.removeObserver(observerCityColor)
    }

    @Test
    fun `onViewCreated should fetch reminder from interactor`() {
        val wasteTypeId = 1
        val reminder = WasteCalendarReminder(
            wasteTypeId = wasteTypeId,
            remindTime = "08:00",
            sameDay = true,
            oneDayBefore = false,
            twoDaysBefore = false
        )
        every { wasteCalendarInteractor.findReminder(wasteTypeId) } returns Single.just(reminder)

        viewModel.onViewCreated(wasteTypeId)

        verify { observerReminder.onChanged(reminder) }
    }

    @Test
    fun `onViewResumedOrSettingsApplied should request notification settings`() {
        viewModel.onViewResumedOrSettingsApplied()
        verify { notificationsStateController.requestNotificationSettings() }
    }

    @Test
    fun `onReminderDone should save new reminder and track event if notification is enabled`() {
        val wasteTypeId = 1
        val reminder = WasteCalendarReminder(
            wasteTypeId = wasteTypeId,
            remindTime = "08:00",
            sameDay = true,
            oneDayBefore = false,
            twoDaysBefore = true
        )
        every { wasteCalendarInteractor.saveReminder(reminder) } just Runs
        viewModel.onReminderDone(
            wasteTypeId = wasteTypeId,
            time = "08:00",
            sameDay = true,
            dayBefore = false,
            twoDaysBefore = true
        )
        verify { wasteCalendarInteractor.saveReminder(reminder) }
        verify { adjustManager.trackEvent(any()) }
    }
}
