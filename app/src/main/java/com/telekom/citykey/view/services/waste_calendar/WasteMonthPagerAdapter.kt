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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.services.waste_calendar

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WastecalendarMonthPageBinding
import com.telekom.citykey.models.waste_calendar.WasteCalendarPickups
import com.telekom.citykey.utils.extensions.inflateChild
import java.util.*

class WasteMonthPagerAdapter(private val dateClickListener: (Date) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentDate = Calendar.getInstance()
    private val currMonth = currentDate.get(Calendar.MONTH)
    private val currYear = currentDate.get(Calendar.YEAR)
    var availableMonths = 12 - currMonth
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MonthViewHolder(WastecalendarMonthPageBinding.bind(parent.inflateChild(R.layout.wastecalendar_month_page)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MonthViewHolder) {
            val newMonth = currMonth + position
            if (newMonth > 11) holder.binding.monthView.setDate(currYear + 1, newMonth - 12)
            else holder.binding.monthView.setDate(currYear, newMonth)
            holder.binding.monthView.onDateSelected(dateClickListener)
        }
    }

    override fun getItemCount() = availableMonths

    internal class MonthViewHolder(val binding: WastecalendarMonthPageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setPickups(items: List<WasteCalendarPickups>, cityColor: Int) {
            binding.monthView.setPrimaryColor(cityColor)
            binding.monthView.setPickups(items)
        }
    }
}
