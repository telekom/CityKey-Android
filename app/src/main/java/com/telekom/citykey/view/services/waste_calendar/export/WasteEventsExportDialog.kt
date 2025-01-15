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

package com.telekom.citykey.view.services.waste_calendar.export

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WasteEventsExportDialogBinding
import com.telekom.citykey.models.waste_calendar.CalendarAccount
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import com.telekom.citykey.view.services.waste_calendar.export.options.WasteExportSelectionDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class WasteEventsExportDialog(private val calendarAccounts: List<CalendarAccount>) :
    FullScreenBottomSheetDialogFragment(R.layout.waste_events_export_dialog) {
    private val binding by viewBinding(WasteEventsExportDialogBinding::bind)
    private val viewModel: WasteEventsExportViewModel by viewModel()
    private var listAdapter: WasteEventsExportAdapter? = null
    private var calendarAcc: CalendarAccount = calendarAccounts[0]

    companion object {
        const val FRAGMENT_TAG_EXPORT = "waste_export"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
    }

    fun subscribeUi() {
        viewModel.appliedFilters.observe(viewLifecycleOwner) {
            binding.toolbar.menu.findItem(R.id.add).isVisible = it.isNotEmpty()
            listAdapter?.submitList(it)
        }
        viewModel.eventsExported.observe(viewLifecycleOwner) {
            DialogUtil.showInfoDialog(
                requireContext(),
                R.string.wc_006_successfully_exported,
                R.string.wc_006_alert_message_successfull,
                okBtnClickListener = { dismiss() }
            )
        }
        viewModel.error.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
        }
    }

    private fun initViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        (binding.toolbar.getChildAt(0) as TextView).textSize = 18f
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.add) {
                viewModel.onAddClicked(calendarAcc)
            }
            false
        }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        binding.chooseCalendarContainer.setAccessibilityRole(AccessibilityRole.Button)


        listAdapter = WasteEventsExportAdapter()
        setCalendarDetail()
        binding.categoryList.adapter = listAdapter
        binding.chooseCalendarContainer.setOnClickListener {
            WasteExportSelectionDialog(calendarAccounts, calendarAcc) {
                calendarAcc = it
                setCalendarDetail()
            }.showDialog(
                parentFragmentManager,
                FRAGMENT_TAG_EXPORT
            )
        }
    }

    private fun setCalendarDetail() {
        binding.calendarName.text = calendarAcc.calendarDisplayName
        binding.chooseCalendarContainer.contentDescription = calendarAcc.calendarDisplayName
        (binding.sideColor.background as LayerDrawable)
            .findDrawableByLayerId(R.id.mainLayer)
            .colorFilter = PorterDuffColorFilter(calendarAcc.calendarColor, PorterDuff.Mode.SRC_IN)
        listAdapter?.sideColor = calendarAcc.calendarColor
    }

    override fun dismiss() {
        listAdapter = null
        super.dismiss()
    }
}
