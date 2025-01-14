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

import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.MonthItemBinding
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.isSameDayAs
import java.util.*

class MonthAdapter(private val selectionListener: (DateSelection) -> Unit) :
    RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    private val currentDate = Calendar.getInstance()
    private val currMonth = currentDate.get(Calendar.MONTH)
    private val currYear = currentDate.get(Calendar.YEAR)
    private var primaryColor = 0

    private val selection = DateSelection(null, null, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder =
        MonthViewHolder(MonthItemBinding.bind(parent.inflateChild(R.layout.month_item)))

    override fun getItemCount() = 13
    override fun getItemId(position: Int) = position.toLong()
    override fun getItemViewType(position: Int) = 1

    fun updateSelection(newSelection: DateSelection) {
        selection.start = newSelection.start
        selection.end = newSelection.end
        selection.single = selection.start.isSameDayAs(selection.end)
        notifyDataSetChanged()
    }

    fun setPrimaryColor(@ColorInt color: Int) {
        primaryColor = color
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.binding.monthView.setSelection(selection)
        holder.binding.monthView.setPrimaryColor(primaryColor)
        holder.binding.monthView.onDateSelected {
            when {
                selection.start == null -> {
                    selection.start = it
                    selection.end = it
                    selection.single = true
                }
                selection.start.isSameDayAs(it) -> {
                    selection.start = null
                    selection.end = null
                    selection.single = false
                }
                selection.start != null && selection.end != null && !selection.single -> {
                    selection.end = it
                    selection.start = it
                    selection.single = true
                }
                else -> {
                    if (it.before(selection.start)) {
                        selection.end = selection.start
                        selection.start = it
                    } else {
                        selection.end = it
                    }
                    selection.single = false
                }
            }
            selectionListener.invoke(selection)
            notifyDataSetChanged()
        }
        val newMonth = currMonth + position
        if (newMonth > 11) holder.binding.monthView.setDate(currYear + 1, newMonth - 12)
        else holder.binding.monthView.setDate(currYear, newMonth)
    }

    inner class MonthViewHolder(val binding: MonthItemBinding) : RecyclerView.ViewHolder(binding.root)
}
