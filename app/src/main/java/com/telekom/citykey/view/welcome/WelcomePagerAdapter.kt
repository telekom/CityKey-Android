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

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomeGuideFragmentBinding
import com.telekom.citykey.networkinterface.models.welcome.WelcomePageItem
import com.telekom.citykey.utils.extensions.inflateChild

class WelcomePagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val pagesData: List<WelcomePageItem> = arrayListOf(
        WelcomePageItem(
            imgRes = R.drawable.onboarding1,
            description = R.string.x_001_welcome_info_01,
            title = R.string.x_001_welcome_info_title_01
        ),
        WelcomePageItem(
            imgRes = R.drawable.onboarding2,
            description = R.string.x_001_welcome_info_02,
            title = R.string.x_001_welcome_info_title_02
        ),
        WelcomePageItem(
            imgRes = R.drawable.onboarding3,
            description = R.string.x_001_welcome_info_03,
            title = R.string.x_001_welcome_info_title_03
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        WelcomePageViewHolder(WelcomeGuideFragmentBinding.bind(parent.inflateChild(R.layout.welcome_guide_fragment)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WelcomePageViewHolder).setPageData(pagesData[position])
    }

    override fun getItemCount(): Int = pagesData.size

    private inner class WelcomePageViewHolder(
        val binding: WelcomeGuideFragmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setPageData(pageData: WelcomePageItem) {
            binding.welcomeImage.setImageResource(pageData.imgRes)
            binding.descHeading.setText(pageData.title)
            binding.description.setText(pageData.description)
        }
    }
}
