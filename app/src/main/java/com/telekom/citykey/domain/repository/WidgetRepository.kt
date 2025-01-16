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

package com.telekom.citykey.domain.repository

import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.api.contracts.CitykeyWidgetApi
import com.telekom.citykey.models.api.contracts.CitykeyWidgetAuthApi
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.extensions.isInPast
import com.telekom.citykey.utils.extensions.isToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetRepository(
    private val citykeyWidgetApi: CitykeyWidgetApi,
    private val citykeyWidgetAuthApi: CitykeyWidgetAuthApi,
    private val preferencesHelper: PreferencesHelper
) {

    val currentCityId get() = preferencesHelper.getSelectedCityId()

    suspend fun getNewsForCurrentCity(count: Int): NewsState = withContext(Dispatchers.IO) {
        try {
            val oscaResponse = citykeyWidgetApi.getNewsForCityContent(currentCityId)
            val filteredList =
                if (oscaResponse.content.size > count) oscaResponse.content.take(count) else oscaResponse.content
            NewsState.Success(filteredList)
        } catch (e: Exception) {
            NewsState.Error
        }
    }

    suspend fun getSelectedWastePickupIds() = withContext(Dispatchers.IO) {
        try {
            val oscaResponse = citykeyWidgetAuthApi.getWasteCalendarPickupIds(currentCityId)
            oscaResponse.content.wasteTypeIds
            //   NewsState.Success()
        } catch (e: Exception) {
            e
        }
    }

    suspend fun getWasteCalendarData() = withContext(Dispatchers.IO) {
        try {
            val oscaResponse = citykeyWidgetAuthApi.getSelectedWastePickupsData(
                WasteCalendarRequest("", ""),
                currentCityId
            )
            oscaResponse.content.calendar.filter { !it.date.isInPast || it.date.isToday }.take(3)
        } catch (e: Exception) {
            e
        }
    }
}
