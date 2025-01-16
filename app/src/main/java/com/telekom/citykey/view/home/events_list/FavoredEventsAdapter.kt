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

package com.telekom.citykey.view.home.events_list

import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EventsListFavoritesHeaderBinding
import com.telekom.citykey.databinding.EventsListItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.city.events.EventsListItem
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.getShortMonthName
import com.telekom.citykey.utils.extensions.getShortWeekDay
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromURL
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.toCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FavoredEventsAdapter : ListAdapter<EventsListItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<EventsListItem>() {
            override fun areItemsTheSame(oldItem: EventsListItem, newItem: EventsListItem): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: EventsListItem, newItem: EventsListItem): Boolean =
                oldItem == newItem
        }

        private const val TYPE_EVENT = 0
        private const val TYPE_HEADER = 1
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position) is EventsListItem.Header) TYPE_HEADER else TYPE_EVENT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_EVENT)
            EventViewHolder(EventsListItemBinding.bind(parent.inflateChild(R.layout.events_list_item)))
        else
            HeaderViewHolder(EventsListFavoritesHeaderBinding.bind(parent.inflateChild(R.layout.events_list_favorites_header)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                holder.bindItem((getItem(position) as EventsListItem.EventItem).event)
            }

            is HeaderViewHolder -> {
                holder.binding.root.setText((getItem(position) as EventsListItem.Header).title)
                holder.binding.yourFavoritesTitle.setAccessibilityRole(AccessibilityRole.Heading)
            }
        }
    }

    private inner class HeaderViewHolder(val binding: EventsListFavoritesHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    private inner class EventViewHolder(val binding: EventsListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(item: Event) {
            binding.thumbnail.loadFromURL(item.thumbnail)
            binding.title.text = item.title
            binding.title.setAccessibilityRole(AccessibilityRole.Link)
            if (!item.locationName.isNullOrBlank()) {
                binding.location.visibility = View.VISIBLE
                binding.location.text = item.locationName
            }
            binding.eventDate.text =
                if (item.isSingleDay) oneDayDateFormat(item) else twoDaysDateFormat(item)
            binding.eventDateCard.setCardBackgroundColor(CityInteractor.cityColorInt)

            if (item.isCancelled) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_cancelled_events)
                }
                binding.title.paintFlags = binding.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else if (item.isSoldOut) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_events_sold_out_label)
                }
            } else if (item.isPostponed) {
                binding.eventStatus.apply {
                    backgroundTintList = binding.root.resources.getColorStateList(R.color.postponedColor, null)
                    setVisible(true)
                    setText(R.string.e_007_events_new_date_label)
                }
                binding.eventStatus.setText(R.string.e_007_events_new_date_label)
            }
            if (!item.isSingleDay) {
                binding.eventDate.contentDescription =
                    SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.getDefault()).format(
                        item.startDate ?: Date()
                    ) + " to " + SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.getDefault()).format(
                        item.endDate ?: Date()
                    )
            }
            binding.root.setOnClickListener {
                it.findNavController()
                    .navigate(EventsListDirections.actionEventsListToEventDetails().apply { event = item })
            }
            binding.favSign.apply {
                setColorFilter(CityInteractor.cityColorInt)
                setVisible(item.isFavored)
                if (item.isFavored) {
                    contentDescription = context.getString(R.string.e_005_favourite)
                }
            }
        }

        private fun oneDayDateFormat(item: Event) =
            SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.getDefault()).format(item.startDate ?: Date())

        private fun twoDaysDateFormat(item: Event): String {
            val startCalendar = item.startDate.toCalendar()
            val endCalendar = item.endDate.toCalendar()

            val startYear = startCalendar.get(Calendar.YEAR)
            val endYear = endCalendar.get(Calendar.YEAR)

            val startYearString = if (startYear == endYear)
                "" else "$startYear "

            val start = String.format(
                "%s, %s. %s %s",
                startCalendar.getShortWeekDay().replace(".", ""),
                startCalendar.get(Calendar.DAY_OF_MONTH).toString(),
                startCalendar.getShortMonthName().replace(".", ""),
                startYearString
            )

            val end = String.format(
                "%s, %s. %s %s",
                endCalendar.getShortWeekDay().replace(".", ""),
                endCalendar.get(Calendar.DAY_OF_MONTH),
                endCalendar.getShortMonthName().replace(".", ""),
                endYear.toString()
            )

            return String.format("%s- %s", start, end)
        }
    }
}
