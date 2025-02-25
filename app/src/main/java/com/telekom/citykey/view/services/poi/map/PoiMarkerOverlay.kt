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

package com.telekom.citykey.view.services.poi.map

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiMarkerOverlayBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.poi.PointOfInterest
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.decodeHTML
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.openMapApp
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding

class PoiMarkerOverlay(
    private val poiData: PointOfInterest,
    private val categoryName: String,
    private val resultListener: (Boolean) -> Unit
) : BottomSheetDialogFragment() {
    private val binding by viewBinding(PoiMarkerOverlayBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AuthDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.poi_marker_overlay, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val resultDialogFragment = super.onCreateDialog(savedInstanceState)
        setFullHeight(resultDialogFragment)
        return resultDialogFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarPoiCategory.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarPoiCategory.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarPoiCategory.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbarPoiCategory.setNavigationOnClickListener {
            dismiss()
        }
        binding.toolbarPoiCategory.title = categoryName
        binding.addressLabel.setVisible(poiData.address.isNotEmpty())
        binding.address.setVisible(poiData.address.isNotEmpty())
        binding.locationNavigation.setVisible(poiData.address.isNotEmpty())
        binding.locationNavigation.setAccessibilityRole(AccessibilityRole.Button)
        binding.openingHours.setVisible(poiData.openHours.isNotEmpty())
        binding.openingHours.setLinkTextColor(CityInteractor.cityColorInt)
        binding.openingHoursLabel.setVisible(poiData.openHours.isNotEmpty())
        binding.categoryTitle.text = poiData.title
        binding.categorySubTitle.text = poiData.subtitle
        binding.categorySubTitle.setVisible(poiData.subtitle.isNotBlank())
        binding.openingHours.text = poiData.openHours.decodeHTML()
        binding.address.text = poiData.address
        binding.categoryGroupIcon.loadFromDrawable(poiData.categoryGroupIconId)
        binding.categoryGroupIcon.setColorFilter(CityInteractor.cityColorInt)
        binding.categoryContainer.apply {
            setAccessibilityRole(AccessibilityRole.Button)
            contentDescription = poiData.title + poiData.subtitle
            setOnClickListener {
                resultListener.invoke(true)
                dismiss()
            }
        }
        binding.locationNavigation.setTextColor(CityInteractor.cityColorInt)
        binding.locationNavigation.setOnClickListener {
            openMapApp(poiData.latitude, poiData.longitude)
        }
    }

    private fun setFullHeight(dialogFragment: Dialog) {
        dialogFragment.setOnShowListener { dialog ->
            val params = binding.root.layoutParams as FrameLayout.LayoutParams
            params.height = (Resources.getSystem().displayMetrics.heightPixels * 0.4).toInt()
            binding.root.layoutParams = params

            val bottomSheetDialog: BottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheetLayout: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheetLayout!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}
