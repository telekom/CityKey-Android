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

package com.telekom.citykey.domain.city

import android.annotation.SuppressLint
import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.models.content.City
import com.telekom.citykey.utils.PreferencesHelper
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class CityInteractor(
    private val cityRepository: CityRepository, private val preferencesHelper: PreferencesHelper
) {

    companion object {
        var cityColorInt = 0
    }

    private val _city: BehaviorSubject<City> = BehaviorSubject.create()
    val city: Observable<City> = _city.hide()

    val currentCityId get() = _city.value?.cityId ?: -1
    val cityColor get() = Color.parseColor(_city.value?.cityColor ?: "")
    val cityName get() = _city.value?.cityName ?: ""
    val cityLocation get() = _city.value?.location ?: LatLng(0.0, 0.0)

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        city.subscribe {
            cityColorInt = it.cityColorInt
            preferencesHelper.setSelectedCityId(it.cityId)
            preferencesHelper.setSelectedCityName(it.cityName)
        }
    }

    fun loadCity(cityId: Int = currentCityId): Maybe<City> {
        return if (!preferencesHelper.isFirstTime) {
            cityRepository.getCity(cityId).doOnSuccess(_city::onNext)
        } else {
            Maybe.empty()
        }
    }

    fun loadCity(availableCity: AvailableCity): Maybe<City> =
        cityRepository.getCity(availableCity.cityId, availableCity).doOnSuccess(_city::onNext)
}