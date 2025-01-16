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

package com.telekom.citykey.models.content

import java.util.Date

data class CityWeather(
    val atmosphericPressure: Int,
    val cityId: Int,
    val cityKey: Int,
    val cityName: String,
    val cloudiness: Int,
    val clouds: Int,
    val description: String,
    val humidity: Int,
    val maximumTemperature: Double,
    val minimumTemperature: Double,
    val pressure: Int,
    val rain: Int,
    val rainVolume: Int,
    val sunrise: Date? = null,
    val sunset: Date? = null,
    val temp: Double,
    val tempMax: Double,
    val tempMin: Double,
    val temperature: Double,
    val visibility: Int,
    val weatherCondition: String,
    val windDeg: Int,
    val windDirection: Int,
    val windSpeed: Number
)
