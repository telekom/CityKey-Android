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

import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.utils.extensions.tryParsingColor

data class City(
    val cityId: Int,
    val cityName: String? = null,
    val cityColor: String? = null,
    val stateName: String? = null,
    val country: String? = null,
    val cityPicture: String? = null,
    val cityPreviewPicture: String? = null,
    val cityNightPicture: String? = null,
    val servicePicture: String? = null,
    val municipalCoat: String? = null,
    val serviceDesc: String? = null,
    val imprintDesc: String? = null,
    val imprintImage: String? = null,
    val imprintLink: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val postalCode: String? = null,
    val cityConfig: CityConfig? = null
) {
    val cityColorInt: Int get() = tryParsingColor(cityColor)
    val location: LatLng get() = LatLng(latitude, longitude)
}
