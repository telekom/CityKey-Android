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

package com.telekom.citykey.view.dialogs

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DataPrivacySettingsDialogBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.applySafeAllInsetsWithSides
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.android.ext.android.inject

class DataPrivacySettingsDialog(
    private val acceptedListener: (() -> Unit)? = null,
    private val isLaunchedFromNotice: Boolean = false
) : FullScreenBottomSheetDialogFragment(R.layout.data_privacy_settings_dialog) {

    private val adjustManager: AdjustManager by inject()

    private val binding: DataPrivacySettingsDialogBinding by viewBinding(DataPrivacySettingsDialogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        handleWindowInsets()
    }

    private fun initViews() {

        // Toolbar
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        // A11y
        setAccessibilityRoles()

        // Transitions
        binding.scrollView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.requiredToolsBlock.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.optionalToolsBlock.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        // Toggle
        binding.optionalToolsToggle.isChecked = adjustManager.isAnalyticsEventTrackingAllowed

        // Link click
        binding.dataSecurityNoticeLink.setOnClickListener {
            if (isLaunchedFromNotice) {
                dismiss()
            } else {
                DataPrivacyNoticeDialog(
                    isLaunchedFromSettings = true
                ).showDialog(parentFragmentManager, "DataPrivacyNoticeDialog")
            }
        }

        // Buttons
        binding.btnAcceptAll.setOnClickListener {
            adjustManager.updateTrackingPermissions(true)
            acceptedListener?.invoke()
            dismiss()
        }

        binding.btnChangeSettings.setupOutlineStyle()
        binding.btnChangeSettings.setOnClickListener {
            adjustManager.updateTrackingPermissions(binding.optionalToolsToggle.isChecked)
            acceptedListener?.invoke()
            dismiss()
        }

        binding.requiredToolsShowMore.setOnClickListener {
            binding.requiredToolsDescription.updateState()
            binding.requiredToolsShowMore.setText(
                if (binding.requiredToolsDescription.isCollapsed) {
                    R.string.dialog_dpn_settings_show_more_btn
                } else {
                    R.string.dialog_dpn_settings_show_less_btn
                }
            )
        }

        binding.optionalToolsShowMore.setOnClickListener {
            binding.optionalToolsDescription.updateState()
            binding.optionalToolsShowMore.setText(
                if (binding.optionalToolsDescription.isCollapsed) {
                    R.string.dialog_dpn_settings_show_more_btn
                } else {
                    R.string.dialog_dpn_settings_show_less_btn
                }
            )
        }
    }

    private fun handleWindowInsets() {
        binding.appBarLayout.applySafeAllInsetsWithSides(left = true, right = true)
        binding.scrollView.applySafeAllInsetsWithSides(left = true, right = true, bottom = true)
    }

    private fun setAccessibilityRoles() {
        binding.requiredToolsShowMore.setAccessibilityRole(AccessibilityRole.Button)
        binding.optionalToolsShowMore.setAccessibilityRole(AccessibilityRole.Button)
        binding.dataSecurityNoticeLink.setAccessibilityRole(AccessibilityRole.Button)
        binding.textHeader.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
        binding.requiredToolsHeader.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
        binding.optionalToolsHeader.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
    }
}
