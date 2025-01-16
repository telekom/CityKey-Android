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

package com.telekom.citykey.models.live_data

import androidx.annotation.ColorInt
import com.telekom.citykey.models.content.City
import com.telekom.citykey.utils.extensions.tryParsingColor
import com.telekom.citykey.view.home.HomeViewTypes

data class HomeData(
    val city: String?,
    val municipalCoat: String?,
    @ColorInt val cityColor: Int,
    val viewTypes: List<Int>
) {
    companion object {
        fun fromCity(city: City): HomeData {
            val viewTypes = mutableListOf<Int>()
            viewTypes.add(HomeViewTypes.VIEW_TYPE_NEWS)
            viewTypes.add(HomeViewTypes.VIEW_TYPE_EVENTS)
//            if (city.cityConfig.showHomeTips) viewTypes.add(HomeViewTypes.VIEW_TYPE_TIPS)
//            if (city.cityConfig.showHomeOffers) viewTypes.add(HomeViewTypes.VIEW_TYPE_OFFERS)
//            if (city.cityConfig.showHomeDiscounts) viewTypes.add(HomeViewTypes.VIEW_TYPE_DISCOUNTS)

            return HomeData(
                city.cityName,
                city.municipalCoat,
                tryParsingColor(city.cityColor),
                viewTypes
            )
        }
    }
}
