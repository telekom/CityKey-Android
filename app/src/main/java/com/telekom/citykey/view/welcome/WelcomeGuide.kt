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

package com.telekom.citykey.view.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomeGuideBinding
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.viewBinding

class WelcomeGuide : Fragment(R.layout.welcome_guide) {

    private val binding by viewBinding(WelcomeGuideBinding::bind)
    private var welcomeAdapter: WelcomePagerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        welcomeAdapter = WelcomePagerAdapter()
        binding.pager.apply {
            adapter = welcomeAdapter
            TabLayoutMediator(binding.pageIndicator, binding.pager) { _, _ -> }.attach()
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position < welcomeAdapter!!.itemCount - 1)
                        binding.pagerNextBtn.setText(R.string.x_001_next_btn_text)
                    else
                        binding.pagerNextBtn.setText(R.string.x_001_lets_go_btn_text)
                }
            })
        }
        binding.pagerNextBtn.setOnClickListener {
            val position = binding.pager.currentItem
            if (position < welcomeAdapter!!.itemCount - 1) {
                binding.pager.currentItem = position + 1
            } else {
                findNavController().navigate(WelcomeGuideDirections.actionWelcomeGuideToWelcome())
            }
        }
        binding.pagerNextBtn.setAccessibilityRole(AccessibilityRole.Button)
    }

    override fun onDestroy() {
        super.onDestroy()
        welcomeAdapter = null
    }
}
