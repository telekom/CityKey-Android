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

package com.telekom.citykey.domain.widget

import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.domain.repository.WidgetRepository
import com.telekom.citykey.models.Pickups
import com.telekom.citykey.models.WasteItems
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.models.waste_calendar.WasteCalendarPickups
import com.telekom.citykey.utils.extensions.isDayAfterTomorrow
import com.telekom.citykey.utils.extensions.isToday
import com.telekom.citykey.utils.extensions.isTomorrow
import kotlinx.coroutines.runBlocking


class WidgetInteractor(private val widgetRepository: WidgetRepository) {
    private var _newsList: MutableList<CityContent> = mutableListOf()
    private var _newsListMutableLiveData = MutableLiveData<List<CityContent>>()

    private var _wastePickupIdList: MutableList<Int> = mutableListOf()
    private var _wastePickupIdListMutableLiveData = MutableLiveData<List<Int>>()

    private var _todaysPickups: MutableList<WasteItems.WasteItem> = mutableListOf()
    private var _tomorrowPickups: MutableList<WasteItems.WasteItem> = mutableListOf()
    private var _DATPickups: MutableList<WasteItems.WasteItem> = mutableListOf()

    private var _pickups: Pickups = Pickups(_todaysPickups, _tomorrowPickups, _DATPickups)
    private var _pickupsMutableLiveData = MutableLiveData<Pickups>()

    val currentCityId get() = widgetRepository.currentCityId
    val newsList: List<CityContent> get() = _newsList
    var pickups: Pickups = _pickups
    private var _errorType = MutableLiveData<String>()
    var errorType = _errorType

    fun clearNewsList() {
        _newsList.clear()
        _newsListMutableLiveData.postValue(_newsList)
    }

    fun clearWasteList() {
        _todaysPickups.clear()
        _tomorrowPickups.clear()
        _DATPickups.clear()
    }

    fun getNewsForCurrentCity(isSingleItemWidget: Boolean) {
        runBlocking {
            _newsList.clear()
            val newsState = widgetRepository.getNewsForCurrentCity(if (isSingleItemWidget) 1 else 3)
            if (newsState is NewsState.Success) {
                _newsList.apply {
                    clear()
                    addAll(newsState.content)
                }
            }
            _newsListMutableLiveData.postValue(_newsList)
        }
    }

    private fun getSelectedWastePickupIds() {
        runBlocking {
            _wastePickupIdList.clear()
            try {
                val wastePickups = widgetRepository.getSelectedWastePickupIds() as List<Int>
                _wastePickupIdList.apply {
                    clear()
                    addAll(wastePickups)
                }
            } catch (e: Exception) {
            }
        }
        _wastePickupIdListMutableLiveData.postValue(_wastePickupIdList)
    }


    fun getWasteCalenderData(isSingleItemWidget: Boolean) {
        getSelectedWastePickupIds()
        runBlocking {
            _todaysPickups.clear()
            _tomorrowPickups.clear()
            _DATPickups.clear()
            try {
                val wasteData = widgetRepository.getWasteCalendarData() as MutableList<WasteCalendarPickups>
                for (pickup in wasteData) {
                    if (pickup.date.isToday) {
                        _todaysPickups.addAll(pickup.wasteTypeList.filter { it.wasteTypeId in _wastePickupIdList }
                            .sortedBy { it.wasteType })
                        continue
                    }
                    if (pickup.date.isTomorrow()) {
                        _tomorrowPickups.addAll(pickup.wasteTypeList.filter { it.wasteTypeId in _wastePickupIdList }
                            .sortedBy { it.wasteType })
                        continue
                    }
                    if (pickup.date.isDayAfterTomorrow()) {
                        _DATPickups.addAll(pickup.wasteTypeList.filter { it.wasteTypeId in _wastePickupIdList }
                            .sortedBy { it.wasteType })
                        continue
                    }
                    _errorType.postValue("success")
                }
            } catch (e: Exception) {
                errorType.postValue("exception")
            }
            _pickups = Pickups(_todaysPickups, _tomorrowPickups, _DATPickups)
        }
        _pickupsMutableLiveData.postValue(_pickups)
    }
}
