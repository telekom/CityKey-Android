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

package com.telekom.citykey.view.home.events_list.date_filter

import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.calendar.DateSelection
import com.telekom.citykey.databinding.EventsDateFilterBinding
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormatSymbols

class DateFilter : FullScreenBottomSheetDialogFragment(R.layout.events_date_filter) {
    private val viewModel: DateFilterViewModel by viewModel()
    private val binding by viewBinding(EventsDateFilterBinding::bind)

    private val clearMenuItem: MenuItem by lazy {
        binding.toolbarDateFilter.menu.findItem(R.id.actionClearAll)
    }
    private var isSuccess = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarDateFilter.inflateMenu(R.menu.events_date_filter_menu)
        binding.toolbarDateFilter.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarDateFilter.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbarDateFilter.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarDateFilter.setNavigationOnClickListener { dismiss() }
        binding.toolbarDateFilter.setOnMenuItemClickListener {
            binding.calendar.setSelectedDates(DateSelection(null, null, false))
            viewModel.onDateSelectedChange(null)
            clearMenuItem.isVisible = false
            true
        }
        setAccessibilityRoleForToolbarTitle(binding.toolbarDateFilter)

        val weekdays = DateFormatSymbols().weekdays
        binding.sundayTitle.text = weekdays[1]
        binding.sundayTitle.contentDescription = weekdays[1]
        binding.mondayTitle.text = weekdays[2]
        binding.mondayTitle.contentDescription = weekdays[2]
        binding.tuesdayTitle.text = weekdays[3]
        binding.tuesdayTitle.contentDescription = weekdays[3]
        binding.wednesdayTitle.text = weekdays[4]
        binding.wednesdayTitle.contentDescription = weekdays[4]
        binding.thursdayTitle.text = weekdays[5]
        binding.thursdayTitle.contentDescription = weekdays[5]
        binding.fridayTitle.text = weekdays[6]
        binding.fridayTitle.contentDescription = weekdays[6]
        binding.saturdayTitle.text = weekdays[7]
        binding.saturdayTitle.contentDescription = weekdays[7]

        binding.calendar.onDateSelected {
            viewModel.onDateSelectedChange(it)
            clearMenuItem.isVisible = it != null
        }

        binding.progressBtnShowEvents.setOnClickListener {
            viewModel.confirmFiltering()
            isSuccess = true
            dismiss()
        }

        observeUi()
    }

    private fun observeUi() {
        viewModel.color.observe(viewLifecycleOwner) {
            binding.calendar.setPrimaryColor(it)
            binding.progressBtnShowEvents.button.setBackgroundColor(it)
            clearMenuItem.title = SpannableString(clearMenuItem.title).apply {
                setSpan(ForegroundColorSpan(it), 0, this.length, 0)
            }
        }

        viewModel.eventsCount.observe(viewLifecycleOwner) {
            val buttonText = if (it == null) getString(R.string.e_003_show_events_button)
            else String.format("%s (%d)", getString(R.string.e_003_show_events_button), it)
            binding.progressBtnShowEvents.text = buttonText
        }

        viewModel.dateSelection.observe(viewLifecycleOwner) {
            it?.let { selection -> binding.calendar.setSelectedDates(selection) }
            clearMenuItem.isVisible = it != null
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!isSuccess) viewModel.revokeFiltering()
        super.onDismiss(dialog)
    }
}
