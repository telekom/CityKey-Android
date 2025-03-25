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

package com.telekom.citykey.utils.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updatePadding

val View.isNotVisible: Boolean get() = visibility == View.GONE || visibility == View.INVISIBLE

fun View.setVisible(visible: Boolean, mode: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else mode
}

fun View.disable() {
    isEnabled = false
    animate().alpha(0.3f).setDuration(200).start()
}

fun View.enable() {
    isEnabled = true
    animate().alpha(1f).setDuration(200).start()
}

fun View.setAllEnabled(enabled: Boolean) {
    isEnabled = enabled
    if (this is ViewGroup) children.forEach { child -> child.setAllEnabled(enabled) }
}

/**
 * Applies the window insets from all the sides to this [View], so that this View is not obstructed by System Bars
 * + Display cutouts i.e. Status bar, Navigation bar & Camera punch-holes, WaterDrop/Pill notches etc.
 */
fun View.applySafeAllInsets() = applySafeAllInsetsWithSides(left = true, top = true, right = true, bottom = true)

/**
 * Applies the window insets from the selected sides to this [View], so that this View is not obstructed by System Bars
 * + Display cutouts i.e. Status bar, Navigation bar & Camera punch-holes, WaterDrop/Pill notches etc.
 * From which sides to apply the insets can be decided by controlling the 4 parameters
 */
fun View.applySafeAllInsetsWithSides(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
) {

    val (initialLeft, initialTop, initialRight, initialBottom) = listOf(
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom
    )

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(
            left = initialLeft + if (left) bars.left else 0,
            top = initialTop + if (top) bars.top else 0,
            right = initialRight + if (right) bars.right else 0,
            bottom = initialBottom + if (bottom) bars.bottom else 0
        )
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * Dispatches window insets to the specified child views and performs an optional extra action.
 *
 * @param childViews The child views to which the insets should be dispatched.
 * @param extraActionToPerform An optional lambda function to perform additional actions with the insets.
 */
fun View.dispatchInsetsToChildViews(
    vararg childViews: View,
    extraActionToPerform: ((Insets) -> Unit)? = null
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->

        val safeInsets =
            insets.getInsets(WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars())

        childViews.onEach {
            it.updatePadding(
                left = safeInsets.left,
                right = safeInsets.right
            )
        }

        extraActionToPerform?.invoke(safeInsets)

        WindowInsetsCompat.CONSUMED
    }
}
