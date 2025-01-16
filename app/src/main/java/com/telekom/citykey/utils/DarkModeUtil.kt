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

package com.telekom.citykey.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.telekom.citykey.R
import timber.log.Timber

val Resources.isDarkMode get() =
    configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun GoogleMap.tryLoadingNightStyle(context: Context) {
    try {
        val success = setMapStyle(
            MapStyleOptions.loadRawResourceStyle(context, R.raw.night_mode_style)
        )
        if (success.not()) {
            Timber.e("Style parsing for dark mode failed.")
        }
    } catch (e: Resources.NotFoundException) {
        Timber.e("Can't find map style for dark mode. Error: ", e)
    }
}
