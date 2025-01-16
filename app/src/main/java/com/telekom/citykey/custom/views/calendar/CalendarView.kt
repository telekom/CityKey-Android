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

package com.telekom.citykey.custom.views.calendar

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CalendarView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    private var selectionListener: ((DateSelection?) -> Unit)? = null

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = MonthAdapter {
            if (it.start == null) selectionListener?.invoke(null)
            else selectionListener?.invoke(it)
        }
    }

    fun onDateSelected(listener: (DateSelection?) -> Unit) {
        selectionListener = listener
    }

    fun setSelectedDates(selection: DateSelection) {
        (adapter as MonthAdapter).updateSelection(selection)
    }

    fun setPrimaryColor(@ColorInt color: Int) {
        (adapter as MonthAdapter).setPrimaryColor(color)
    }
}
