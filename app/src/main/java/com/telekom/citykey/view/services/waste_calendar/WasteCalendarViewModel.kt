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

package com.telekom.citykey.view.services.waste_calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.services.surveys.SurveysState
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.live_data.WasteCalendarData
import com.telekom.citykey.models.waste_calendar.CalendarAccount
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.BackpressureStrategy
import java.util.*

class WasteCalendarViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    private val _appliedFilters: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    private val _appliedAddress: MutableLiveData<String> = MutableLiveData()

    val appliedFilters: LiveData<Pair<Int, Int>> get() = _appliedFilters
    val appliedAddress: LiveData<String> get() = _appliedAddress
    private val _calendarAccounts: SingleLiveEvent<List<CalendarAccount>> = SingleLiveEvent()
    val calendarAccounts: LiveData<List<CalendarAccount>> get() = _calendarAccounts
    val updateSelectedWasteCount: LiveData<String> get() = wasteCalendarInteractor.updateSelectedWasteCount

    val wasteData: LiveData<WasteCalendarData>
        get() = wasteCalendarInteractor.monthlyDataSubject
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    val userLoggedOut: LiveData<Unit>
        get() = globalData.user
            .filter { it is UserState.Absent }
            .map { Unit }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    fun onViewCreated() {
        _appliedFilters.postValue(wasteCalendarInteractor.filters.size to wasteCalendarInteractor.categoriesSize)
        _appliedAddress.postValue(wasteCalendarInteractor.fullAddress)
    }

    fun onMonthChanged(calendar: Calendar) {
        wasteCalendarInteractor.getDataForSelectedMonth(calendar)
    }

    fun onStreetUpdated() {
        _appliedAddress.postValue(wasteCalendarInteractor.fullAddress)
    }

    fun onExportBtnClicked() {
        launch {
            wasteCalendarInteractor.getCalendarsInfo()
                .subscribe(_calendarAccounts::postValue)
        }
    }

    fun onCategoriesUpdated() {
        _appliedFilters.postValue(wasteCalendarInteractor.filters.size to wasteCalendarInteractor.categoriesSize)
    }
}
