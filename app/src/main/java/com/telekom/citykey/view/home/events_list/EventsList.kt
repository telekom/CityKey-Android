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

package com.telekom.citykey.view.home.events_list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EventsListFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.applySafeAllInsetsWithSides
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.getShortMonthName
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.toCalendar
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.home.events_list.category_filter.CategoryFilter
import com.telekom.citykey.view.home.events_list.date_filter.DateFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

@ExperimentalCoroutinesApi
class EventsList : MainFragment(R.layout.events_list_fragment) {

    private val viewModel: EventsListViewModel by viewModel()
    private val binding by viewBinding(EventsListFragmentBinding::bind)

    private var listAdapter: EventsListAdapter? = null
    private var favoritesAdapter: FavoredEventsAdapter? = null
    private var loadStateAdapter: EventsLoadStateAdapter? = null
    private var refreshStateAdapter: EventsLoadStateAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadStateAdapter = EventsLoadStateAdapter { listAdapter?.retry() }
        refreshStateAdapter = EventsLoadStateAdapter { listAdapter?.retry() }

        favoritesAdapter = FavoredEventsAdapter()

        listAdapter = EventsListAdapter().also {
            it.addLoadStateListener { loadStates ->
                viewModel.onLoadStateChanged(loadStates)
                refreshStateAdapter?.loadState = loadStates.refresh
                loadStateAdapter?.loadState = loadStates.append
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        handleWindowInsets()
        subscribeUi()
    }

    private fun initViews() {
        setupToolbar(binding.toolbarEvents)

        binding.eventsList.adapter = ConcatAdapter(favoritesAdapter, refreshStateAdapter, listAdapter, loadStateAdapter)

        binding.dateFilterBtn.setOnClickListener {
            DateFilter()
                .showDialog(childFragmentManager)
        }
        binding.categoryFilterBtn.setOnClickListener {
            CategoryFilter()
                .showDialog(childFragmentManager)
        }
        binding.categoryFilters.setAccessibilityRole(AccessibilityRole.Button)
        binding.dateFilters.setAccessibilityRole(AccessibilityRole.Button)
    }

    override fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.eventsListABL.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )

            insets
        }
        binding.eventsList.applySafeAllInsetsWithSides(
            bottom = true,
            left = true,
            right = true
        )
    }

    @SuppressLint("DefaultLocale")
    private fun subscribeUi() {

        viewModel.pagingData.observe(viewLifecycleOwner) {
            listAdapter?.submitData(lifecycle, it)
        }

        viewModel.clearLoadedEvents.observe(viewLifecycleOwner) {
            listAdapter?.submitData(lifecycle, PagingData.empty())
        }

        viewModel.activeDateFilter.observe(viewLifecycleOwner) { filter ->
            binding.dateFilters.setTextColor(if (filter?.start == null) getColor(R.color.onSurfaceSecondary) else CityInteractor.cityColorInt)
            binding.dateFilters.setText(R.string.e_002_filter_empty_label)
            filter?.let {
                val calendarStart = it.start?.toCalendar()
                val calendarEnd = it.end?.toCalendar()
                val dateFiltersText = String.format(
                    "%d %s - %d %s",
                    calendarStart?.get(Calendar.DAY_OF_MONTH),
                    calendarStart?.getShortMonthName(),
                    calendarEnd?.get(Calendar.DAY_OF_MONTH),
                    calendarEnd?.getShortMonthName()
                )

                binding.dateFilters.text = dateFiltersText
            }
        }

        viewModel.activeCategoryFilter.observe(viewLifecycleOwner) { filters ->
            binding.categoryFilters.setTextColor(if (filters.isNullOrEmpty()) getColor(R.color.onSurfaceSecondary) else CityInteractor.cityColorInt)

            if (filters.isNullOrEmpty()) {
                binding.categoryFilters.setText(R.string.e_002_filter_empty_label)
            } else {
                binding.categoryFilters.text = filters
                    .filter { it.categoryName.isNullOrBlank().not() }
                    .joinToString(separator = ", ") { it.categoryName!! }
            }
        }

        viewModel.favoredEvents.observe(viewLifecycleOwner) {
            favoritesAdapter?.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
        favoritesAdapter = null
        loadStateAdapter = null
        refreshStateAdapter = null
    }
}
