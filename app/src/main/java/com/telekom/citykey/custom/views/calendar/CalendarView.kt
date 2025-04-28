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

package com.telekom.citykey.custom.views.calendar

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A custom view that displays a calendar using a RecyclerView.
 *
 * @constructor Creates a CalendarView with the specified context, attributes, and style.
 * @param context The context of the view.
 * @param attrs The attribute set for the view.
 * @param defStyleAttr The default style attribute for the view.
 */
class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var selectionListener: ((DateSelection?) -> Unit)? = null

    /**
     * Retrieves the current primary color of the calendar.
     *
     * @return The primary color as an integer.
     */
    private fun getCurrentPrimaryColor(): Int = adapter?.let {
        (it as? MonthAdapter)?.primaryColor
    } ?: 0

    init {
        layoutManager = LinearLayoutManager(context)
        setup()
    }

    /**
     * Called when the configuration of the device changes.
     *
     * @param newConfig The new device configuration.
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        setup()
    }

    /**
     * Sets up the layout manager and adapter for the RecyclerView.
     */
    private fun setup() {

        val initialPrimaryColor: Int = getCurrentPrimaryColor()

        adapter = MonthAdapter {
            if (it.start == null) {
                selectionListener?.invoke(null)
            } else {
                selectionListener?.invoke(it)
            }
        }

        if (initialPrimaryColor != 0) {
            setPrimaryColor(initialPrimaryColor)
        }
    }

    /**
     * Sets a listener to be invoked when a date is selected.
     *
     * @param listener The listener to be invoked.
     */
    fun onDateSelected(listener: (DateSelection?) -> Unit) {
        selectionListener = listener
    }

    /**
     * Sets the selected dates in the calendar.
     *
     * @param selection The selected dates.
     */
    fun setSelectedDates(selection: DateSelection) {
        (adapter as MonthAdapter).updateSelection(selection)
    }

    /**
     * Sets the primary color of the calendar.
     *
     * @param color The primary color.
     */
    fun setPrimaryColor(@ColorInt color: Int) {
        (adapter as MonthAdapter).setPrimaryColor(color)
    }
}
