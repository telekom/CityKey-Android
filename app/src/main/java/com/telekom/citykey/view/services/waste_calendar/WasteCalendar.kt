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

package com.telekom.citykey.view.services.waste_calendar

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WastecalendarOverviewFragmentBinding
import com.telekom.citykey.databinding.WeekRowWasteCalendarBinding
import com.telekom.citykey.models.WasteItems
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.hasPermission
import com.telekom.citykey.utils.extensions.isInLandscapeOrientation
import com.telekom.citykey.utils.extensions.safeRun
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.toCalendar
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.waste_calendar.address_change.WasteCalendarAddress
import com.telekom.citykey.view.services.waste_calendar.address_change.WasteCalendarAddress.Companion.FRAGMENT_TAG_ADDRESS
import com.telekom.citykey.view.services.waste_calendar.export.WasteEventsExportDialog
import com.telekom.citykey.view.services.waste_calendar.filters.WasteFilters
import com.telekom.citykey.view.services.waste_calendar.filters.WasteFilters.Companion.FRAGMENT_TAG_FILTERS
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

class WasteCalendar : MainFragment(R.layout.wastecalendar_overview_fragment) {

    private val viewModel: WasteCalendarViewModel by viewModel()
    private val binding by viewBinding(WastecalendarOverviewFragmentBinding::bind)

    private var listAdapter: WasteAdapter? = null
    private var pagerAdapter: WasteMonthPagerAdapter? = null
    private val args: WasteCalendarArgs by navArgs()

    private fun WeekRowWasteCalendarBinding.assignTexts() {
        val weekdays = DateFormatSymbols().shortWeekdays
        this.sundayTitle.text = weekdays[1]
        this.mondayTitle.text = weekdays[2]
        this.tuesdayTitle.text = weekdays[3]
        this.wednesdayTitle.text = weekdays[4]
        this.thursdayTitle.text = weekdays[5]
        this.fridayTitle.text = weekdays[6]
        this.saturdayTitle.text = weekdays[7]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.weekRowInToolbar.assignTexts()
        binding.weekRowAboveCalendar.assignTexts()

        binding.addressFilters.setAccessibilityRole(AccessibilityRole.Button)
        binding.categoryFilters.setAccessibilityRole(AccessibilityRole.Button)
        binding.pagerNextButton.setAccessibilityRole(AccessibilityRole.Button)
        binding.pagerPrevButton.setAccessibilityRole(AccessibilityRole.Button)

        listAdapter = WasteAdapter()
        pagerAdapter = WasteMonthPagerAdapter {
            val smoothScroller = object : LinearSmoothScroller(requireContext()) {
                override fun getVerticalSnapPreference() = SNAP_TO_START
            }

            smoothScroller.targetPosition = listAdapter?.currentList?.indexOfFirst { item ->
                item is WasteItems.DayItem && item.date.toCalendar().get(Calendar.DAY_OF_MONTH) == it.toCalendar()
                    .get(Calendar.DAY_OF_MONTH)
            } ?: return@WasteMonthPagerAdapter
            if (smoothScroller.targetPosition == -1) return@WasteMonthPagerAdapter

            binding.appBarLayout.setExpanded(false)
            binding.wasteList.layoutManager?.startSmoothScroll(smoothScroller)
        }
        binding.wasteList.adapter = listAdapter
        binding.pager.adapter = pagerAdapter

        binding.pagerNextButton.setOnClickListener {
            binding.pager.currentItem = binding.pager.currentItem + 1
        }

        binding.pagerPrevButton.setOnClickListener {
            binding.pager.currentItem = binding.pager.currentItem - 1
        }

        binding.addressFilterBtn.setOnClickListener {
            WasteCalendarAddress { isSuccess ->
                if (isSuccess) {
                    val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, binding.pager.currentItem) }
                    viewModel.onMonthChanged(calendar)
                    viewModel.onStreetUpdated()
                    binding.pickupsFilterBtn.performClick()
                }
            }.showDialog(childFragmentManager, FRAGMENT_TAG_ADDRESS)
        }

        binding.pickupsFilterBtn.setOnClickListener {
            openPickupSelectionFilter()
        }

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, position) }
                viewModel.onMonthChanged(calendar)
                val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                val year = calendar.get(Calendar.YEAR)
                binding.monthNameText.text = "$monthName $year"

                when (position) {
                    0 -> {
                        binding.pagerPrevButton.disable()
                        binding.pagerNextButton.enable()
                    }

                    pagerAdapter!!.itemCount - 1 -> {
                        binding.pagerNextButton.disable()
                        binding.pagerPrevButton.enable()
                    }

                    else -> {
                        binding.pagerPrevButton.enable()
                        binding.pagerNextButton.enable()
                    }
                }
            }
        })

        viewModel.onViewCreated()
        setupToolbar(binding.toolbarWasteCalendar)
        binding.toolbarWasteCalendar.title = args.service

        if (args.hasPickupExist)
            openPickupSelectionFilter()

        if (args.shouldNavigateToNextMonth) {
            binding.pager.currentItem = binding.pager.currentItem + 1
        }
        subscribeUi()

        if (requireActivity().intent.data != null) {
            requireActivity().intent.data = null
        }

        adjustLayoutByOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustLayoutByOrientation()
    }

    private fun subscribeUi() {

        viewModel.wasteData.observe(viewLifecycleOwner) {
            binding.filterSelectionText.setVisible(it.listItems.isEmpty())
            pagerAdapter?.availableMonths = it.availableMonths
            listAdapter?.submitList(it.listItems)
            binding.pager.post {
                safeRun {
                    (
                            (binding.pager[0] as RecyclerView).findViewHolderForAdapterPosition(binding.pager.currentItem)
                                    as? WasteMonthPagerAdapter.MonthViewHolder
                            )
                        ?.setPickups(it.monthItems, it.cityColor)
                }
            }

            if (it.availableMonths == 1) {
                binding.pagerPrevButton.disable()
                binding.pagerNextButton.disable()
            }
        }

        viewModel.appliedFilters.observe(viewLifecycleOwner) {
            when (it.first) {
                0 -> {
                    binding.categoryFilters.setText(R.string.wc_004_filter_category_nothing_selected)
                    binding.filterSelectionText.setText(R.string.wc_004_filter_category_empty)
                }

                it.second -> {
                    binding.categoryFilters.setText(R.string.wc_004_filter_category_All_selected)
                    binding.filterSelectionText.text =
                        resources.getQuantityString(R.plurals.wc_004_filter_category_empty_data, it.first)
                }

                else -> {
                    binding.filterSelectionText.text =
                        resources.getQuantityString(R.plurals.wc_004_filter_category_empty_data, it.first)
                    binding.categoryFilters.text = getString(R.string.wc_004_filter_category_selected_count, it.first)
                }
            }
        }

        viewModel.appliedAddress.observe(viewLifecycleOwner) {
            if (it.isEmpty()) binding.addressFilters.setText(R.string.e_002_filter_empty_label)
            else binding.addressFilters.text = it
        }

        viewModel.userLoggedOut.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.services, false)
        }
        viewModel.calendarAccounts.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                WasteEventsExportDialog(it).showDialog(childFragmentManager)
            } else {
                DialogUtil.showInfoDialog(
                    requireContext(),
                    R.string.wc_006_no_calendar_available_for_export_title,
                    R.string.wc_006_no_calendar_available_for_export_message
                )
            }
        }

        viewModel.updateSelectedWasteCount.observe(viewLifecycleOwner) {
            val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, binding.pager.currentItem) }
            viewModel.onMonthChanged(calendar)
            viewModel.onCategoriesUpdated()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionExport -> {
                if (isPermissionGranted()) {
                    viewModel.onExportBtnClicked()
                } else {
                    requestPermission()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.waste_export_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isPermissionGranted ->
            if (isPermissionGranted[Manifest.permission.READ_CALENDAR] == true && isPermissionGranted[Manifest.permission.WRITE_CALENDAR] == true) {
                viewModel.onExportBtnClicked()
            }
        }

    private fun isPermissionGranted() =
        requireContext().hasPermission(Manifest.permission.WRITE_CALENDAR)

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null
        pagerAdapter = null
    }

    private fun openPickupSelectionFilter() {
        WasteFilters { isSuccess ->
            if (isSuccess) {
                val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, binding.pager.currentItem) }
                viewModel.onMonthChanged(calendar)
                viewModel.onCategoriesUpdated()
            }
        }.showDialog(childFragmentManager, FRAGMENT_TAG_FILTERS)
    }

    val Int.px get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    /**
     * Since we cannot load a new layout or perform a context change by ourselves, we change the fields' constraints
     * in the existing ConstraintLayout to match the portrait mode
     */
    private fun createPortraitLayout() {

        val constraintLayout: ConstraintLayout = binding.contentLayout
        val set = ConstraintSet()
        set.clone(constraintLayout)

        // Calendar
        set.connect(binding.svCalendar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(binding.svCalendar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(binding.svCalendar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(binding.svCalendar.id, ConstraintSet.BOTTOM, binding.wasteList.id, ConstraintSet.TOP)

        // List
        set.connect(binding.wasteList.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(binding.wasteList.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(binding.wasteList.id, ConstraintSet.TOP, binding.svCalendar.id, ConstraintSet.BOTTOM)
        set.connect(binding.wasteList.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        set.applyTo(constraintLayout)

        binding.weekRowAboveCalendar.root.isVisible = false
        binding.weekRowInToolbar.root.isVisible = true
    }

    /**
     * Since we cannot load a new layout or perform a context change by ourselves, we change the fields' constraints
     * in the existing ConstraintLayout to match the landscape mode
     */
    private fun createLandscapeLayout() {

        val constraintLayout: ConstraintLayout = binding.contentLayout
        val set = ConstraintSet()
        set.clone(constraintLayout)

        // Calendar
        set.connect(binding.svCalendar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(binding.svCalendar.id, ConstraintSet.END, binding.wasteList.id, ConstraintSet.START)
        set.connect(binding.svCalendar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(binding.svCalendar.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        set.setHorizontalWeight(binding.svCalendar.id, 1f)

        // List
        set.connect(binding.wasteList.id, ConstraintSet.START, binding.svCalendar.id, ConstraintSet.END)
        set.connect(binding.wasteList.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(binding.wasteList.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        set.connect(binding.wasteList.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        set.setHorizontalWeight(binding.wasteList.id, 1f)

        set.applyTo(constraintLayout)

        binding.weekRowAboveCalendar.root.isVisible = true
        binding.weekRowInToolbar.root.isVisible = false
    }

    /**
     * Change required layout when an Orientation change is detected
     */
    private fun adjustLayoutByOrientation() {
        if (isInLandscapeOrientation) {
            createLandscapeLayout()
        } else {
            createPortraitLayout()
        }
    }
}
