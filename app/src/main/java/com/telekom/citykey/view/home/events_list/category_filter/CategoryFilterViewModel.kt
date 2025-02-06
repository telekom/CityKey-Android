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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.home.events_list.category_filter

import androidx.lifecycle.LiveData
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.content.EventCategory
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import timber.log.Timber

class CategoryFilterViewModel(
    private val globalData: GlobalData,
    private val cityRepository: CityRepository,
    private val eventsInteractor: EventsInteractor
) : NetworkingViewModel() {
    val allCategories: LiveData<List<EventCategory>> get() = _allCategories
    val filters: LiveData<ArrayList<Int>> get() = _filters
    val eventsCount: LiveData<Int?> get() = eventsInteractor.eventsCount

    private val _allCategories: SingleLiveEvent<List<EventCategory>> = SingleLiveEvent()
    private val _filters: SingleLiveEvent<ArrayList<Int>> = SingleLiveEvent()

    private var selectedCategories: ArrayList<Int> = arrayListOf()

    init {

        launch {
            cityRepository.getAllEventCategories(globalData.currentCityId)
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        _allCategories.postValue(it)
                        loadFilters()
                        if (eventsCount.value == null) refreshEventsCount()
                    },
                    this::onError
                )
        }
    }

    private fun loadFilters() {
        _filters.postValue(eventsInteractor.categoryIdFilters.value)
    }

    private fun refreshEventsCount() {
        launch {
            eventsInteractor.refreshEventsCount(categories = selectedCategories)
                .subscribe(eventsInteractor::updateEventsCount, Timber::e)
        }
    }

    fun onCategoryAdded(id: Int) {
        selectedCategories.add(id)
        refreshEventsCount()
    }

    fun onCategoryRemoved(id: Int) {
        selectedCategories.remove(id)
        refreshEventsCount()
    }

    fun onFiltersCleared() {
        selectedCategories.clear()
        refreshEventsCount()
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> {
                showRetry()
            }
            else -> _filters.postValue(arrayListOf())
        }
    }

    fun revokeFiltering() {
        eventsInteractor.revokeEventsCount()
    }

    fun confirmFiltering() {
        val eventsCategories = arrayListOf<EventCategory>()
        selectedCategories.forEach { id ->
            allCategories.value?.find {
                it.id == id
            }?.let { category -> eventsCategories.add(category) }
        }

        eventsInteractor.updateCategories(selectedCategories, eventsCategories)
        eventsInteractor.applyFilters()
    }
}
