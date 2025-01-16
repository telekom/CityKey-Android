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

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.telekom.citykey.R

fun BottomNavigationView.setItemsColor(color: Int) {
    val colors: IntArray = intArrayOf(ContextCompat.getColor(context, R.color.onSurfaceSecondary), color)
    val bottomNavigationStates: Array<IntArray> = arrayOf(
        intArrayOf(-android.R.attr.state_checked), // not pressed
        intArrayOf(android.R.attr.state_checked) // pressed
    )
    val colorStates = ColorStateList(bottomNavigationStates, colors)
    itemIconTintList = colorStates
    itemTextColor = colorStates
}
