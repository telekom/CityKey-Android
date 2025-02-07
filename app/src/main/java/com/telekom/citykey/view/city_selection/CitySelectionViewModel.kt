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

package com.telekom.citykey.view.city_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationBasedCitiesInteractor
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CitySelectionViewModel(
    private val globalData: GlobalData,
    private val preferencesHelper: PreferencesHelper,
    private val availableCitiesInteractor: AvailableCitiesInteractor,
    private val locationBasedCitiesInteractor: LocationBasedCitiesInteractor,
    private val adjustManager: AdjustManager
) : NetworkingViewModel() {

    val contentAll: LiveData<List<Cities>> get() = _contentAll
    val cityUpdated: LiveData<Boolean> get() = _cityUpdated
    val isCityActive: LiveData<Unit> get() = _isCityActive

    private val _isCityActive: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _contentAll: MutableLiveData<List<Cities>> = MutableLiveData()
    private val _cityUpdated: MutableLiveData<Boolean> = MutableLiveData()
    private val availableCities = mutableListOf<AvailableCity>()
    private var cityItemNearest: Cities = Cities.Progress

    init {
        loadAvailableCities()
    }

    fun selectCity(availableCity: AvailableCity) {
        launch {
            globalData.loadCity(availableCity)
                .retryOnError(
                    this::onError,
                    retryDispatcher,
                    pendingRetries
                )
                .subscribe({
                    adjustManager.trackEvent(R.string.switch_city)
                    _cityUpdated.postValue(true)
                }, ::onError)
        }
    }

    private fun loadAvailableCities() {
        launch {
            availableCitiesInteractor.availableCities
                .subscribeOn(Schedulers.io())
                .map(this::setSelectedCity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        availableCities.clear()
                        availableCities.addAll(it)
                        updateCitiesContent()
                    },
                    this::onError
                )
        }
    }

    private fun updateCitiesContent() {
        val cities: MutableList<Cities> = availableCities.map { availableCity -> Cities.City(availableCity) }
            .toMutableList()
        cities.add(0, Cities.Header(R.string.c_002_cities_location_header))
        cities.add(1, cityItemNearest)
        cities.add(2, Cities.Header(R.string.c_002_city_selection_list_header))
        _contentAll.postValue(cities)
    }

    fun onPermissionsMissing() {
        cityItemNearest = Cities.NoPermission
        updateCitiesContent()
    }

    fun onNearestCityRequested() {
        launch {
            locationBasedCitiesInteractor.getNearestCity()
                .flatMap { nearestCity ->
                    val availableCity = availableCities.find { nearestCity.cityId == it.cityId }
                    return@flatMap if (availableCity == null) {
                        Maybe.error(Exception())
                    } else {
                        availableCity.distance = nearestCity.distance
                        Maybe.just(availableCity)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    { availableCity ->
                        cityItemNearest = Cities.NearestCity(availableCity)
                        updateCitiesContent()
                    },
                    this::onLocationBasedCitiesError
                )
        }
    }

    fun onLocationServicesDisabled() {
        cityItemNearest = Cities.Error
        updateCitiesContent()
    }

    private fun setSelectedCity(cities: List<AvailableCity>): List<AvailableCity> {
        val selectedCityId = preferencesHelper.getSelectedCityId()

        cities.find { it.cityId == selectedCityId }?.let {
            cities.forEach { city -> city.isSelected = false }
            it.isSelected = true
        }
        return cities
    }

    private fun onLocationBasedCitiesError(throwable: Throwable) {
        cityItemNearest = Cities.Error
        updateCitiesContent()
        onError(throwable)
    }

    private fun onError(throwable: Throwable) {
        cityItemNearest = Cities.Error
        updateCitiesContent()
        when (throwable) {
            is NoConnectionException -> {
                showRetry()
            }
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.SERVICE_NOT_ACTIVE -> {
                            _isCityActive.postValue(Unit)
                        }
                    }
                }
            }
            else -> {
                _technicalError.postValue(Unit)
            }
        }
    }

    fun getUpdatedCitiesList(emailSupportIsAvailable: Boolean, cities: List<Cities>) =
        if (emailSupportIsAvailable && cities.size > 3) {
            cities.toMutableList().apply { add(Cities.ContactLink) }
        } else {
            cities
        }

}
