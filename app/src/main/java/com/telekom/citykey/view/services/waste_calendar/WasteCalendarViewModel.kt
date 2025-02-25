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
