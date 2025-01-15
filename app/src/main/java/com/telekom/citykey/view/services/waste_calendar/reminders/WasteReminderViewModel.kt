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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.services.waste_calendar.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.notifications.NotificationsStateController
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.waste_calendar.WasteCalendarReminder
import com.telekom.citykey.view.BaseViewModel

class WasteReminderViewModel(
    globalData: GlobalData,
    private val wasteCalendarInteractor: WasteCalendarInteractor,
    private val notificationsStateController: NotificationsStateController,
    private val adjustManager: AdjustManager
) : BaseViewModel() {

    private val _reminder: MutableLiveData<WasteCalendarReminder> = MutableLiveData()
    private val _cityColor: MutableLiveData<Int> = MutableLiveData(globalData.cityColor)

    val reminder: LiveData<WasteCalendarReminder> get() = _reminder
    val cityColor: LiveData<Int> get() = _cityColor
    val areOsNotificationsEnabled: LiveData<Boolean> get() = cityColor.switchMap { notificationsStateController.areOsNotificationsEnabled }

    fun onViewCreated(wasteTypeId: Int) {
        launch {
            wasteCalendarInteractor.findReminder(wasteTypeId)
                .subscribe(_reminder::postValue)
        }
    }

    fun onViewResumedOrSettingsApplied() {
        notificationsStateController.requestNotificationSettings()
    }

    fun onReminderDone(wasteTypeId: Int, time: String, sameDay: Boolean, dayBefore: Boolean, twoDaysBefore: Boolean) {
        val reminder = WasteCalendarReminder(wasteTypeId, time, sameDay, dayBefore, twoDaysBefore)
        if (reminder != _reminder.value) {
            wasteCalendarInteractor.saveReminder(reminder)
            if (reminder.sameDay || reminder.oneDayBefore || reminder.twoDaysBefore)
                adjustManager.trackEvent(R.string.set_waste_reminder)
        }
    }
}
