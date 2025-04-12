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

package com.telekom.citykey.view.home

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.custom.ItemDecorationBetweenOnly
import com.telekom.citykey.databinding.HomeEventsBinding
import com.telekom.citykey.databinding.HomeNewsBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.city.events.EventsHomeData
import com.telekom.citykey.domain.city.events.EventsState
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.fadeIn
import com.telekom.citykey.utils.extensions.fadeOut
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.view.home.feed_adapters.EventsFeedAdapter
import com.telekom.citykey.view.home.feed_adapters.NewsFeedAdapter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FeedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var cityColor = 0
    private var eventsViewHolder: EventsViewHolder? = null
    private var eventsNetworkState: EventsState = EventsState.RUNNING

    private var newsFeedAdapter: NewsFeedAdapter = NewsFeedAdapter()
    private var eventsFeedAdapter: EventsFeedAdapter = EventsFeedAdapter()

    private var eventsPosition = 0
    private var yourEvents: List<Event>? = null

    private var viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    private val viewTypes = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HomeViewTypes.VIEW_TYPE_NEWS -> NewsViewHolder(HomeNewsBinding.bind(parent.inflateChild(R.layout.home_news)))
            else -> EventsViewHolder(HomeEventsBinding.bind(parent.inflateChild(R.layout.home_events)))
                .also { eventsViewHolder = it }
        }
    }

    override fun getItemCount() = viewTypes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EventsViewHolder) {
            eventsPosition = holder.absoluteAdapterPosition
            holder.adaptYourEvents(yourEvents)
        }
    }

    fun init(config: List<Int>, cityColor: Int) {
        this.cityColor = cityColor
        yourEvents = null

        viewTypes.clear()
        viewTypes.addAll(config)
        notifyDataSetChanged()
    }

    fun updateNews(state: NewsState) {
        newsFeedAdapter.updateNews(state)
    }

    fun updateEvents(eventsHomeData: EventsHomeData) {
        val events = eventsHomeData.events!!

        eventsFeedAdapter.updateData(events)
        eventsNetworkState = if (events.isEmpty()) {
            EventsState.FAILED
        } else {
            EventsState.SUCCESS
        }
        yourEvents = eventsHomeData.yourEvents
        eventsViewHolder?.adaptYourEvents(yourEvents)
    }

    fun updateEventsState(newState: EventsState) {
        eventsViewHolder?.refreshShowAllLabelColor()
        if (eventsNetworkState != EventsState.SUCCESS) {
            eventsNetworkState = newState
            eventsViewHolder?.toggleShowAllButton(newState)
        }
        eventsFeedAdapter.updateState(newState)
    }

    override fun getItemViewType(position: Int) = viewTypes[position]

    private inner class NewsViewHolder(binding: HomeNewsBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.newsFeed.apply {
                clearOnScrollListeners()
                setRecycledViewPool(viewPool)
                addItemDecoration(
                    ItemDecorationBetweenOnly(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.home_news_item_decoration
                        )!!
                    )
                )
                adapter = newsFeedAdapter
            }
        }
    }

    private inner class EventsViewHolder(val binding: HomeEventsBinding) :
        RecyclerView.ViewHolder(binding.root), KoinComponent {

        private val adjustManager: AdjustManager by inject()

        init {
            binding.labelEvents.apply {
                setAccessibilityRole(
                    AccessibilityRole.Heading,
                    (context.getString(R.string.accessibility_heading_level_2))
                )
            }
            binding.btnShowAll.apply {
                setAccessibilityRole(AccessibilityRole.Button)
                setOnClickListener {
                    adjustManager.trackEvent(R.string.open_events_list)
                    it.findNavController().navigate(HomeDirections.actionHomeToEventsList())
                }
            }
            refreshShowAllLabelColor()
            toggleShowAllButton(eventsNetworkState)
            binding.layoutHomeEvents.layoutTransition.apply {
                enableTransitionType(LayoutTransition.CHANGING)
                setAnimateParentHierarchy(false)
            }

            binding.eventsFeed.apply {
                setRecycledViewPool(viewPool)
                adapter = eventsFeedAdapter
            }
        }

        fun refreshShowAllLabelColor() {
            binding.btnShowAll.setTextColor(
                ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_pressed),
                        intArrayOf(android.R.attr.state_pressed)
                    ),
                    intArrayOf(CityInteractor.cityColorInt, ColorUtils.darken(CityInteractor.cityColorInt, 0.85f))
                )
            )
        }

        fun toggleShowAllButton(state: EventsState) {
            when (state) {
                EventsState.RUNNING, EventsState.FORCELOADING -> binding.btnShowAll.disable()
                EventsState.FAILED, EventsState.EMPTY,
                EventsState.ERRORACTION -> binding.btnShowAll.fadeOut()
                    .subscribe()

                EventsState.SUCCESS -> {
                    binding.btnShowAll.fadeIn().subscribe()
                    binding.btnShowAll.isEnabled = true
                }
            }
        }

        fun adaptYourEvents(events: List<Event>?) {
            if (events.isNullOrEmpty()) {
                binding.yourEventsLayout.setVisible(false)
            } else {
                binding.yourEvents.apply {
                    adapter = EventsFeedAdapter().also {
                        it.updateData(events)
                    }
                }

                binding.yourEventsLayout.setVisible(true)
            }
        }
    }
}
