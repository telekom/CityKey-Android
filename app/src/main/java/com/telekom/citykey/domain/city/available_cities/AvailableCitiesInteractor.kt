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

package com.telekom.citykey.domain.city.available_cities

import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.AvailableCity
import io.reactivex.Maybe

class AvailableCitiesInteractor(private val cityRepository: CityRepository) {

    private val _availableCities: MutableList<AvailableCity> = mutableListOf()

    val availableCities: Maybe<List<AvailableCity>>
        get() =
            if (_availableCities.isEmpty())
                cityRepository.getAllCities()
                    .doOnSuccess { cities ->
                        _availableCities.clear()
                        _availableCities.addAll(cities)
                    }
            else
                Maybe.just(_availableCities)

    fun clearAvailableCities() = _availableCities.clear()
}
