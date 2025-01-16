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

package com.telekom.citykey.view.services.defect_reporter.location_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.view.NetworkingViewModel
import timber.log.Timber

class DefectLocationSelectionViewModel(
    private val globalData: GlobalData,
    private val locationInteractor: LocationInteractor
) : NetworkingViewModel() {
    val cityLocation: LiveData<LatLng> get() = _cityLocation

    val location: LiveData<LatLng?> get() = _location

    private val _location: MutableLiveData<LatLng?> = MutableLiveData()
    private val _cityLocation: MutableLiveData<LatLng> = MutableLiveData(globalData.cityLocation)

    init {
        launch {
            globalData.city.map { it.location }
                .subscribe(_cityLocation::postValue)
        }
    }

    fun onLocationPermissionAvailable() {
        launch {
            locationInteractor.getLocation()
                .subscribe(_location::postValue, Timber::e)
        }
    }
}
