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

package com.telekom.citykey.domain.city.weather

import android.annotation.SuppressLint
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class WeatherInteractor(private val cityRepository: CityRepository, private val globalData: GlobalData) {

    private val _weatherSubject: BehaviorSubject<WeatherState> = BehaviorSubject.create()
    val weatherData: Observable<WeatherState> = _weatherSubject.hide()

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        globalData.city
            .doOnEach { _weatherSubject.onNext(WeatherState.Loading) }
            .flatMap { cityRepository.getWeather(it) }
            .subscribe(_weatherSubject::onNext, Timber::e)
    }
}
