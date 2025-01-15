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

package com.telekom.citykey.view.services.waste_calendar.filters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.models.waste_calendar.SaveSelectedWastePickupRequest
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class WasteFiltersViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor
) : NetworkingViewModel() {

    private val _appliedFilters: MutableLiveData<List<String>> = MutableLiveData()
    private val _categories: MutableLiveData<List<GetWasteTypeResponse>> = MutableLiveData()
    private val _selectedPickupStatus: MutableLiveData<Boolean> = MutableLiveData()

    val appliedFilters: LiveData<List<String>> get() = _appliedFilters
    val categories: LiveData<List<GetWasteTypeResponse>> get() = _categories
    val selectedPickupStatus: LiveData<Boolean> get() = _selectedPickupStatus

    init {
        launch {
            wasteCalendarInteractor.getFilterOptions()
                .map {
                    wasteCalendarInteractor.setCategoriesCount(it)
                    _categories.postValue(it)
                }
                .flatMap {
                    wasteCalendarInteractor.getSelectedWastePickups()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        var applyfilter = ArrayList<String>()
                        it.wasteTypeIds.forEach {
                            applyfilter.add(it.toString())
                        }
                        _appliedFilters.postValue(applyfilter)
                        wasteCalendarInteractor.appliyFilter(applyfilter)
                    },
                    this::onError
                )
        }
    }

    fun saveSelectedWastePickup(saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest) {
        launch {
            wasteCalendarInteractor.saveSelectedWastePickups(saveSelectedWastePickupRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        _selectedPickupStatus.postValue(it.isSuccessful)
                    },
                    this::onError
                )
        }
    }

    fun onCategoryFiltersAccepted(filters: List<String>) {
        var wasteTypeIds = arrayListOf<Int>()
        filters.forEach {
            wasteTypeIds.add(it.toInt())
        }
        saveSelectedWastePickup(SaveSelectedWastePickupRequest(wasteTypeIds))
        wasteCalendarInteractor.setFilters(filters)
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> showRetry()
            else -> _technicalError.postValue(Unit)
        }
    }
}
