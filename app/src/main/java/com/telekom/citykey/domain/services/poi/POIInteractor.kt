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

package com.telekom.citykey.domain.services.poi

import android.annotation.SuppressLint
import com.google.android.gms.maps.model.LatLngBounds
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.models.poi.PoiCategory
import com.telekom.citykey.models.poi.PoiCategoryGroup
import com.telekom.citykey.models.poi.PoiData
import com.telekom.citykey.models.poi.PointOfInterest
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.view.services.poi.PoiState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

@SuppressLint("CheckResult")
class POIInteractor(
    private val servicesRepository: ServicesRepository,
    private val globalData: GlobalData,
    private val prefs: PreferencesHelper,
    private val locationInteractor: LocationInteractor
) {
    val poiData: Observable<PoiData> get() = _poisSubject.hide()
    val activeCategory: Observable<PoiCategory> get() = _activeCategory.hide()
    val poiState: Observable<PoiState> get() = _poiState.hide()

    private val _poisSubject: BehaviorSubject<PoiData> = BehaviorSubject.create()
    private val _activeCategory: BehaviorSubject<PoiCategory> = BehaviorSubject.create()
    private val _poiState: BehaviorSubject<PoiState> = BehaviorSubject.create()

    private val categories = mutableListOf<PoiCategoryGroup>()
    private var isLocationAvailable: Boolean = false
    var selectedCategory: PoiCategory? = null
    private var locationLat: Double = 0.0
    private var locationLong: Double = 0.0

    init {
        globalData.city
            .distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
            .subscribe {
                categories.clear()
                _poisSubject.onNext(PoiData(emptyList(), isLocationAvailable, null, globalData.cityLocation))
                selectedCategory = null
                _poiState.onNext(PoiState.LOADING)
            }
    }

    fun getCategories(): Observable<List<PoiCategoryGroup>> = if (categories.isEmpty()) {
        servicesRepository.getPoiCategories(globalData.currentCityId)
            .doOnSuccess(categories::addAll)
            .toObservable()
    } else {
        Observable.just(categories)
    }

    fun getPois(category: PoiCategory, isInitialLoading: Boolean): Completable =
        if (isInitialLoading) {
            locationInteractor.getLocation()
                .subscribeOn(Schedulers.io())
                .doOnSuccess { isLocationAvailable = true }
                .onErrorReturn {
                    isLocationAvailable = false
                    globalData.cityLocation
                }
                .flatMapMaybe {
                    locationLat = it.latitude
                    locationLong = it.longitude
                    servicesRepository.getPOIs(globalData.currentCityId, locationLat, locationLong, category.categoryId)
                }
                .doOnSubscribe { if (isInitialLoading) _poiState.onNext(PoiState.LOADING) }
                .doOnSuccess { processPois(it, category) }
                .doOnError { if (isInitialLoading) _poiState.onNext(PoiState.ERROR) }
                .ignoreElement()
                .observeOn(AndroidSchedulers.mainThread())
        } else {
            fetchPoisForLastAvailableLocation(category)
        }

    fun getSelectedPoiCategory() = prefs.getPoiCategory(globalData.cityName)

    private fun fetchPoisForLastAvailableLocation(category: PoiCategory): Completable =
        servicesRepository.getPOIs(globalData.currentCityId, locationLat, locationLong, category.categoryId)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { processPois(it, category) }
            .ignoreElement()
            .observeOn(AndroidSchedulers.mainThread())

    private fun processPois(pointOfInterestList: List<PointOfInterest>, category: PoiCategory) {
        pointOfInterestList.forEach { poi -> poi.categoryGroupIcon = category.categoryIcon }
        _poisSubject.onNext(createPoiData(pointOfInterestList))
        _activeCategory.onNext(category)
        selectedCategory = category
        prefs.savePoiCategory(globalData.cityName, category)
        _poiState.onNext(if (pointOfInterestList.isEmpty()) PoiState.EMPTY else PoiState.SUCCESS)
    }

    private fun createPoiData(items: List<PointOfInterest>) =
        when {
            items.size < 50 -> {
                val bounds = LatLngBounds.builder()
                items.forEach { poi -> bounds.include(poi.latLang) }
                PoiData(items, isLocationAvailable, bounds.build(), globalData.cityLocation)
            }
            items.size > 150 -> {
                PoiData(items, isLocationAvailable, null, globalData.cityLocation, 15f)
            }
            else -> {
                PoiData(items, isLocationAvailable, null, globalData.cityLocation)
            }
        }
}
