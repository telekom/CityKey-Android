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

package com.telekom.citykey.view.city_imprint

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.OscaAppBarLayout
import com.telekom.citykey.databinding.CityImprintFragmentBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.network.extensions.cityColorInt
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.pictures.loadFromDrawable
import com.telekom.citykey.pictures.loadFromOSCA
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setAndPerformAccessibilityFocusAction
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.city_selection.CitySelectionFragment
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.profile.ProfileActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CityImprint : Fragment(R.layout.city_imprint_fragment) {

    private val viewModel: CityImprintViewModel by viewModel()
    private val binding by viewBinding(CityImprintFragmentBinding::bind)
    private val adjustManager: AdjustManager by inject()

    private var isUserLoggedIn = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cityImprintToolbar.setAndPerformAccessibilityFocusAction()
        binding.cityImprintToolbar.inflateMenu(R.menu.home_menu)
        setupToolbar(binding.appBarLayout)
        binding.labelCityServices.setAccessibilityRole(AccessibilityRole.Heading)
        handleWindowInsets()
        subscribeUi()
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.containerCityServicesHeader.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = systemInsets.left + 18.dpToPixel(context)
            }

            binding.cityImprintToolbar.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )

            ViewCompat.onApplyWindowInsets(binding.appBarLayout, insets)

            insets
        }
    }

    private fun subscribeUi() {
        viewModel.city.observe(viewLifecycleOwner) {
            binding.toolbarCoat.loadFromOSCA(it.municipalCoat)
            binding.toolbarTitle.text = it.cityName
            binding.cityIcon.loadFromOSCA(it.municipalCoat)
            binding.cityLabel.text = it.cityName
            binding.openImprintButton.button.setBackgroundColor(it.cityColorInt)
            binding.openImprintButton.button.background.setTint(it.cityColorInt)
            binding.content.text = it.imprintDesc
            it.imprintLink?.let { link -> binding.openImprintButton.setOnClickListener { openLink(link.trim()) } }
            with(binding.zoomableImage) {
                if (it.imprintImage.isNullOrBlank().not()) {
                    loadFromOSCA(it.imprintImage!!, R.drawable.imprint_top_image)
                } else {
                    loadFromDrawable(R.drawable.imprint_top_image)
                }
            }
        }

        viewModel.user.observe(viewLifecycleOwner) { isUserLoggedIn = it }
    }

    private fun setupToolbar(appBarLayout: OscaAppBarLayout) {

        appBarLayout.onCollapse { collapsed ->
            val menuItemColor = getColor(if (collapsed) R.color.onSurface else R.color.white)

            binding.toolbarCityBox.setVisible(collapsed)

            binding.cityImprintToolbar.menu.forEach {
                it.icon?.setTint(menuItemColor)
            }
        }

        appBarLayout.findViewById<View>(R.id.actionProfile).setOnClickListener {
            if (isUserLoggedIn) {
                adjustManager.trackEvent(R.string.open_profile)
                startActivity<ProfileActivity>()
            } else {
                startActivity(
                    Intent(it.context, LoginActivity::class.java).apply {
                        putExtra(LoginActivity.LAUNCH_PROFILE, true)
                    }
                )
            }
        }
        appBarLayout.findViewById<View>(R.id.actionSelectCity).setOnClickListener {
            CitySelectionFragment().showDialog(requireActivity().supportFragmentManager)
        }
    }
}
