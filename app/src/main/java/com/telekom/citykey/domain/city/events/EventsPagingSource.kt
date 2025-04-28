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

package com.telekom.citykey.domain.city.events

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.telekom.citykey.custom.views.calendar.DateSelection
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.utils.extensions.toApiFormat
import io.reactivex.Single

class EventsPagingSource(
    private val cityRepository: CityRepository,
    private val globalData: GlobalData,
    private var selectedDates: DateSelection? = null,
    private var categoryFilters: ArrayList<Int>? = null
) : RxPagingSource<Int, Event>() {

    override val keyReuseSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Int, Event>): Int = 1

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Event>> {
        val key = params.key ?: 1

        return cityRepository.getEvents(
            cityId = globalData.currentCityId,
            start = selectedDates?.start?.toApiFormat(),
            end = selectedDates?.end?.toApiFormat(),
            pageNo = key,
            pageSize = params.loadSize,
            categories = categoryFilters
        )
            .map { toLoadResult(it, params) }
            .onErrorReturn { LoadResult.Error(it) }
    }

    private fun toLoadResult(data: List<Event>, params: LoadParams<Int>): LoadResult<Int, Event> {
        val currentKey = params.key ?: 1

        return if (data.isEmpty() && currentKey == 1)
            LoadResult.Error(NoEventsException())
        else
            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = if (data.size < params.loadSize) null else currentKey + 1
            )
    }
}
