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

package com.telekom.citykey.view.services.waste_calendar.export

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.models.waste_calendar.CalendarAccount
import com.telekom.citykey.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.view.NetworkingViewModel
import timber.log.Timber

class WasteEventsExportViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor,
) : NetworkingViewModel() {

    private val _appliedFilters: MutableLiveData<List<GetWasteTypeResponse>> = MutableLiveData(wasteCalendarInteractor.filterCategories)
    val appliedFilters: LiveData<List<GetWasteTypeResponse>> get() = _appliedFilters

    private val _eventsExported: MutableLiveData<Int> = MutableLiveData()
    val eventsExported: LiveData<Int> get() = _eventsExported

    private val _error: MutableLiveData<Unit> = MutableLiveData()
    val error: LiveData<Unit> get() = _error

    fun onAddClicked(calendarAccounts: CalendarAccount) {
        launch {
            wasteCalendarInteractor.exportCalendarEvents(calendarAccounts)
                .subscribe(_eventsExported::setValue, ::onError)
        }
    }

    private fun onError(throwable: Throwable) {
        Timber.e(throwable)
        _error.postValue(Unit)
    }
}
