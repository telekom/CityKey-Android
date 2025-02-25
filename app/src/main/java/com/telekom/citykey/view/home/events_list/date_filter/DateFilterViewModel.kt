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

package com.telekom.citykey.view.home.events_list.date_filter

import androidx.lifecycle.LiveData
import com.telekom.citykey.custom.views.calendar.DateSelection
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.toApiFormat
import com.telekom.citykey.view.BaseViewModel
import timber.log.Timber

class DateFilterViewModel(
    private val eventsInteractor: EventsInteractor,
    globalData: GlobalData
) : BaseViewModel() {

    val dateSelection: LiveData<DateSelection?> get() = _dateSelection
    val color: LiveData<Int> get() = _color
    val eventsCount: LiveData<Int?> get() = eventsInteractor.eventsCount

    private val _dateSelection: SingleLiveEvent<DateSelection?> = SingleLiveEvent()
    private val _color: SingleLiveEvent<Int> = SingleLiveEvent()

    private var selection: DateSelection? = eventsInteractor.selectionDates.value

    init {
        _color.postValue(globalData.cityColor)
        _dateSelection.postValue(selection)

        if (eventsCount.value == null)
            launch {
                eventsInteractor.refreshEventsCount()
                    .subscribe(eventsInteractor::updateEventsCount, Timber::e)
            }
    }

    private fun refreshEventsCount() {
        launch {
            eventsInteractor.refreshEventsCount(
                endDate = selection?.end?.toApiFormat(),
                startDate = selection?.start?.toApiFormat()
            )
                .subscribe(eventsInteractor::updateEventsCount, Timber::e)
        }
    }

    fun onDateSelectedChange(selection: DateSelection?) {
        this.selection = selection
        refreshEventsCount()
    }

    fun revokeFiltering() {
        eventsInteractor.revokeEventsCount()
    }

    fun confirmFiltering() {
        eventsInteractor.updateDates(selection)
        eventsInteractor.applyFilters()
    }
}
