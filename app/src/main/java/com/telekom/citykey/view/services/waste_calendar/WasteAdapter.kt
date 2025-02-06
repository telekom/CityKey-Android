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

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WastecalendarDateItemBinding
import com.telekom.citykey.databinding.WastecalendarWasteItemBinding
import com.telekom.citykey.models.WasteItems
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.isToday
import com.telekom.citykey.utils.extensions.isTomorrow
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.isDarkMode
import java.util.*

class WasteAdapter : ListAdapter<WasteItems, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<WasteItems>() {
            override fun areItemsTheSame(oldItem: WasteItems, newItem: WasteItems): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: WasteItems, newItem: WasteItems): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == R.layout.wastecalendar_waste_item) WasteViewHolder(
            WastecalendarWasteItemBinding.bind(
                parent.inflateChild(
                    viewType
                )
            )
        )
        else DateViewHolder(WastecalendarDateItemBinding.bind(parent.inflateChild(viewType)))

    override fun getItemViewType(position: Int): Int =
        if (getItem(position) is WasteItems.DayItem) R.layout.wastecalendar_date_item else R.layout.wastecalendar_waste_item

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateViewHolder -> holder.setDate(getItem(position))
            is WasteViewHolder -> holder.setWaste(
                getItem(position),
                position == itemCount - 1 ||
                        position < itemCount - 1 && getItem(position + 1) is WasteItems.DayItem
            )
        }
    }

    private class DateViewHolder(val binding: WastecalendarDateItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun setDate(item: WasteItems) {
            if (item is WasteItems.DayItem) {
                val calendar = Calendar.getInstance().apply { time = item.date }
                when {
                    calendar.isToday() -> binding.dateText.setText(R.string.date_format_today_name)
                    item.date.isTomorrow() ->
                        binding.dateText.setText(R.string.date_format_tomorrow_name)

                    else -> {
                        val weekDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

                        binding.dateText.text = binding.root.context.getString(
                            R.string.wc_004_list_item_date_format,
                            weekDay,
                            dayOfMonth,
                            month
                        )
                    }
                }

                binding.root.tag = calendar.get(Calendar.DAY_OF_MONTH)
            }
        }
    }

    private class WasteViewHolder(private val binding: WastecalendarWasteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setWaste(item: WasteItems, dividerVisible: Boolean) {
            if (item is WasteItems.WasteItem) {
                binding.wasteName.text = item.wasteType
                binding.wasteIcon.setColorFilter(
                    if (binding.root.resources.isDarkMode)
                        ColorUtils.invertIfDark(item.wasteIconColorInt)
                    else
                        item.wasteIconColorInt
                )
                binding.divider.setVisible(!dividerVisible)

                binding.root.setOnClickListener {
                    it.findNavController()
                        .navigate(
                            WasteCalendarDirections.actionWasteCalendarToWasteReminder(
                                item.wasteTypeId,
                                item.wasteType
                            )
                        )
                }
                binding.wasteCalendarWasteItemContainer.apply {
                    contentDescription = item.wasteType
                    setAccessibilityRole(AccessibilityRole.Button)
                }
                binding.reminderIcon.setVisible(item.hasReminder)
            }
        }
    }
}
