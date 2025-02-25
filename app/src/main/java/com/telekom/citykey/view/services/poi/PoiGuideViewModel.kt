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

package com.telekom.citykey.view.services.poi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.poi.POIInteractor
import com.telekom.citykey.models.poi.PoiCategory
import com.telekom.citykey.models.poi.PoiData
import com.telekom.citykey.models.poi.PointOfInterest
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class PoiGuideViewModel(
    private val poiInteractor: POIInteractor,
    private val globalData: GlobalData,
    private val locationInteractor: LocationInteractor,
    private val prefs: PreferencesHelper
) : NetworkingViewModel() {

    val userLocation: LiveData<LatLng?> get() = _userLocation
    val launchCategorySelection: LiveData<PoiCategory?> get() = _launchCategorySelection
    val poiData: LiveData<PoiData> get() = _poiData

    val activeCategory: LiveData<PoiCategory> get() = _activeCategory
    val showDetails: LiveData<PointOfInterest> get() = _showDetails
    val isFirstTime: LiveData<Boolean> get() = _isFirstTime
    val poiState: LiveData<PoiState>
        get() = poiInteractor.poiState
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    private val _isFirstTime: MutableLiveData<Boolean> = MutableLiveData()
    private val _poiData: MutableLiveData<PoiData> = MutableLiveData()
    private val _activeCategory: MutableLiveData<PoiCategory> = MutableLiveData()
    private val _userLocation: MutableLiveData<LatLng?> = MutableLiveData()
    private val _launchCategorySelection: SingleLiveEvent<PoiCategory?> = SingleLiveEvent()
    private val _showDetails: SingleLiveEvent<PointOfInterest> = SingleLiveEvent()

    init {
        launch {
            poiInteractor.activeCategory
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_activeCategory::postValue)
        }

        launch {
            poiInteractor.poiData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_poiData::postValue)
        }
    }

    fun onLocationPermissionAvailable() {
        launch {
            locationInteractor.getLocation()
                .subscribe(_userLocation::postValue, Timber::e)
        }
    }

    fun onMarkerClick(position: LatLng?) {
        _poiData.value?.items?.find { it.latLang == position }
            ?.let(_showDetails::setValue)
    }

    fun onRequestPermission() {
        _isFirstTime.value = prefs.getPoiCategory(globalData.cityName) == null
    }

    fun onServiceReady(isRefreshRequired: Boolean = false) {
        val poiCategory = prefs.getPoiCategory(globalData.cityName)
        poiCategory?.let {
            if (it != poiInteractor.selectedCategory || isRefreshRequired) {
                _activeCategory.postValue(it)
                launch {
                    poiInteractor.getPois(it, true)
                        .retryOnError(this::onError, retryDispatcher, pendingRetries, "POIs")
                        .subscribe({ }, Timber::e)
                }
            }
        } ?: _launchCategorySelection.postValue(poiCategory)
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showRetryDialog.call()
            else -> _technicalError.value = Unit
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationInteractor.cancelLocation()
    }

    fun onCategorySelectionRequested() {
        val poiCategory = prefs.getPoiCategory(globalData.cityName)
        _launchCategorySelection.postValue(poiCategory)
    }
}
