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

package com.telekom.citykey.view.services.fahrradparken.existing_reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.fahrradparken.FahrradparkenServiceInteractor
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.fahrradparken.FahrradparkenReport
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import kotlin.math.roundToInt

class FahrradparkenExistingReportsViewModel(
    private val globalData: GlobalData,
    private val locationInteractor: LocationInteractor,
    private val fahrradparkenServiceInteractor: FahrradparkenServiceInteractor
) : NetworkingViewModel() {

    val cityLocation: LiveData<LatLng> get() = _cityLocation

    val location: LiveData<LatLng?> get() = _location

    val fahrradparkenExistingReports: LiveData<List<FahrradparkenReport>>
        get() = _fahrradparkenExistingReports

    val isFetchingReports: Boolean get() = _isFetchingReports

    private val _cityLocation: MutableLiveData<LatLng> = MutableLiveData(globalData.cityLocation)
    private val _location: MutableLiveData<LatLng?> = MutableLiveData()
    private val _fahrradparkenExistingReports = MutableLiveData<List<FahrradparkenReport>>()
    private var _isFetchingReports: Boolean = false

    private val defaultMarkerDensity = 200
    private val markerDensityMap: Map<Int, Int> by lazy {
        mapOf(
            15 to 300,
            14 to 400,
            13 to 500,
            12 to 600,
            11 to 700,
            10 to 800,
            9 to 900,
            8 to 1000,
            7 to 1100,
            6 to 1200,
            5 to 1300,
            4 to 1400,
            3 to 1500,
            2 to 1600,
            1 to 1700
        )
    }

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

    fun getExistingReports(serviceCode: String, boundingBox: String, zoomLevel: Float?) {
        _isFetchingReports = true
        launch {
            fahrradparkenServiceInteractor.getExistingReports(serviceCode, boundingBox, getReportCountLimit(zoomLevel))
                .map { reports ->
                    reports.filter {
                        it.serviceRequestId.isNullOrBlank().not() && it.lat != null && it.lng != null
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _fahrradparkenExistingReports.postValue(it)
                        _isFetchingReports = false
                    },
                    this::onError
                )
        }
    }

    private fun getReportCountLimit(zoomLevel: Float?): Int {
        val density = zoomLevel?.let { markerDensityMap[it.roundToInt()] }
        return density ?: defaultMarkerDensity
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                processOscaErrors(throwable.error as OscaErrorResponse)
            }

            is NoConnectionException -> {
                showRetry()
            }

            else -> {
                _technicalError.value = Unit
            }
        }

        _fahrradparkenExistingReports.postValue(listOf())
        _isFetchingReports = false
        // This is needed to reset the flag for fetching new reports, after the camera is updated by markers population
    }

    private fun processOscaErrors(oscaErrorResponse: OscaErrorResponse) {
        oscaErrorResponse.errors.forEach {
            when (it.errorCode) {
                ErrorCodes.DEFECT_NOT_FOUND -> {
                    _fahrradparkenExistingReports.postValue(listOf())
                }

                else -> {
                    _technicalError.value = Unit
                }
            }
        }
    }

}
