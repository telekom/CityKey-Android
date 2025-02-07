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

package com.telekom.citykey.view.services.waste_calendar.service_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.toLiveData
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.content.CitizenService
import com.telekom.citykey.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers

class WasteCalendarDetailsViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor,
    private val servicesInteractor: ServicesInteractor,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    private val _wasteCalendarAvailable: MutableLiveData<Unit> = SingleLiveEvent()
    private val _launchFtu: MutableLiveData<Unit> = SingleLiveEvent()
    private val _appliedFilters: MutableLiveData<List<String>> = SingleLiveEvent()
    private val _categories: MutableLiveData<List<GetWasteTypeResponse>> = SingleLiveEvent()

    val wasteCalendarAvailable: LiveData<Unit> get() = _wasteCalendarAvailable
    val appliedFilters: LiveData<List<String>> get() = _appliedFilters
    val categories: LiveData<List<GetWasteTypeResponse>> get() = _categories

    val launchFtu: LiveData<Unit> get() = _launchFtu

    val service: LiveData<CitizenService?>
        get() = servicesInteractor.state
            .filter { it !is ServicesStates.Loading }
            .toLiveData()
            .map { state ->
                if (state is ServicesStates.Success)
                    state.data.services.find { it.function == ServicesFunctions.WASTE_CALENDAR }
                else
                    null
            }

    val userLoggedOut: LiveData<Unit>
        get() = globalData.user
            .filter { it is UserState.Absent }
            .map { Unit }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    fun onOpenWasteCalendarClicked() {
        launch {
            wasteCalendarInteractor.getData()
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "wasteData")
                .subscribe(
                    {
                        _wasteCalendarAvailable.postValue(Unit)
                    },
                    this::onError
                )
        }

    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> showRetry()
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.WASTE_CALENDAR_NO_ADDRESS,
                        ErrorCodes.WASTE_CALENDAR_NOT_FOUND,
                        ErrorCodes.WASTE_CALENDAR_WRONG_ADDRESS,
                        ErrorCodes.CALENDAR_NOT_EXIST -> _launchFtu.postValue(Unit)

                        else -> _technicalError.postValue(Unit)
                    }
                }
            }

            else -> _technicalError.postValue(Unit)
        }
    }

    fun getWasteCalendarFilterOptions() {
        launch {
            wasteCalendarInteractor.getFilterOptions()
                .map {
                    wasteCalendarInteractor.setCategoriesCount(it)
                    _categories.postValue(it)
                }
                .flatMap { wasteCalendarInteractor.getSelectedWastePickups() }
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        var applyfilter = ArrayList<String>()
                        it.wasteTypeIds.forEach {
                            applyfilter.add(it.toString())
                        }
                        _appliedFilters.postValue(applyfilter)
                        wasteCalendarInteractor.setFilters(applyfilter)
                    },
                    this::onError
                )
        }
    }

    fun checkSelectedPickupInFilterOptions() = wasteCalendarInteractor.filterCategories
}
