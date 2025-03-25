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

package com.telekom.citykey.view.services.waste_calendar.reminders

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WasteCalendarSetReminderFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.KoverIgnore
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.applySafeAllInsetsWithSides
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setAllEnabled
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

@KoverIgnore
class WasteReminder : MainFragment(R.layout.waste_calendar_set_reminder_fragment) {

    private val viewModel: WasteReminderViewModel by viewModel()
    private val args: WasteReminderArgs by navArgs()
    private val binding by viewBinding(WasteCalendarSetReminderFragmentBinding::bind)

    private val switchStates: Array<IntArray> = arrayOf(
        intArrayOf(-android.R.attr.state_checked),
        intArrayOf(android.R.attr.state_checked)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarWasteReminders)
        viewModel.onViewCreated(args.wasteTypeId)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onReminderDone(
                        args.wasteTypeId,
                        binding.timeText.text.toString(),
                        binding.sameDaySwitch.isChecked,
                        binding.dayBeforeSwitch.isChecked,
                        binding.twoDaysBeforeSwitch.isChecked
                    )
                    findNavController().navigateUp()
                }
            }
        )

        initViews()
        subscribeUi()
    }

    override fun handleWindowInsets() {
        super.handleWindowInsets()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.toolbarWasteReminders.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )
            insets
        }
        binding.nsvWasteCalendarReminder.applySafeAllInsetsWithSides(left = true, right = true, bottom = true)
    }

    private fun initViews() {
        setupToolbar(binding.toolbarWasteReminders)
        viewModel.onViewCreated(args.wasteTypeId)
        binding.toolbarWasteReminders.title = args.wasteType
        binding.buttonOSNotifications.setupNormalStyle(CityInteractor.cityColorInt)
        binding.headerText.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.notesLabel.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.notificationsLabel.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.timeText.setAccessibilityRole(AccessibilityRole.Button)

        val thumbTintList =
            ColorStateList(switchStates, intArrayOf(getColor(R.color.white), CityInteractor.cityColorInt))
        val trackTintList =
            ColorStateList(
                switchStates,
                intArrayOf(getColor(R.color.onSurfaceSecondary), ColorUtils.setAlpha(CityInteractor.cityColorInt, 40))
            )

        binding.sameDaySwitch.thumbTintList = thumbTintList
        binding.sameDaySwitch.trackTintList = trackTintList
        binding.dayBeforeSwitch.thumbTintList = thumbTintList
        binding.dayBeforeSwitch.trackTintList = trackTintList
        binding.twoDaysBeforeSwitch.thumbTintList = thumbTintList
        binding.twoDaysBeforeSwitch.trackTintList = trackTintList

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onViewResumedOrSettingsApplied()
    }

    private fun subscribeUi() {

        viewModel.reminder.observe(viewLifecycleOwner) {
            binding.timeText.text = it.remindTime
            binding.sameDaySwitch.isChecked = it.sameDay
            binding.dayBeforeSwitch.isChecked = it.oneDayBefore
            binding.twoDaysBeforeSwitch.isChecked = it.twoDaysBefore
        }

        viewModel.areOsNotificationsEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.settingsLayout.enable()
                binding.settingsLayout.setAllEnabled(true)
            } else {
                binding.settingsLayout.disable()
                binding.settingsLayout.setAllEnabled(false)
            }
            binding.osNotificationsLayout.setVisible(!it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                viewModel.onReminderDone(
                    args.wasteTypeId,
                    binding.timeText.text.toString(),
                    binding.sameDaySwitch.isChecked,
                    binding.dayBeforeSwitch.isChecked,
                    binding.twoDaysBeforeSwitch.isChecked
                )
                super.onOptionsItemSelected(item)
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {

        binding.timeBox.setOnClickListener {
            val time = binding.timeText.text.toString()
                .split(':')
                .map(String::toInt)

            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(time[0])
                .setMinute(time[1])
                .setTheme(R.style.TimePickerTheme)
                .build()
                .apply {
                    addOnPositiveButtonClickListener {
                        val selectedTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                        binding.timeText.text = selectedTime
                    }
                }
                .show(childFragmentManager, "TimePicker")
        }

        binding.sameDayBox.setOnClickListener {
            binding.sameDaySwitch.isChecked = !binding.sameDaySwitch.isChecked
        }

        binding.dayBeforeBox.setOnClickListener {
            binding.dayBeforeSwitch.isChecked = !binding.dayBeforeSwitch.isChecked
        }

        binding.twoDaysBeforeBox.setOnClickListener {
            binding.twoDaysBeforeSwitch.isChecked = !binding.twoDaysBeforeSwitch.isChecked
        }

        binding.buttonOSNotifications.setOnClickListener {
            val settingsIntent: Intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
            }
            startActivity(settingsIntent)
        }
    }
}
