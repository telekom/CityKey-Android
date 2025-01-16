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

package com.telekom.citykey.utils.extensions

import android.graphics.Color
import androidx.annotation.ColorInt
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.RegExUtils

@ColorInt
fun tryParsingColor(colorString: String?, failsafeColor: String = "#1E73D2"): Int {
    if (colorString.isNullOrBlank()) return Color.parseColor(failsafeColor)
    return try {
        Color.parseColor(if (RegExUtils.hexColor.matcher(colorString).matches()) colorString else failsafeColor)
    } catch (e: Exception) {
        Color.parseColor(failsafeColor)
    }
}

@ColorInt
fun tryParsingColorStringToInt(colorString: String?, failsafeColor: Int = CityInteractor.cityColorInt): Int {
    if (colorString.isNullOrBlank()) return failsafeColor
    return try {
        if (RegExUtils.hexColor.matcher(colorString).matches()) Color.parseColor(colorString) else failsafeColor
    } catch (e: Exception) {
        failsafeColor
    }
}
