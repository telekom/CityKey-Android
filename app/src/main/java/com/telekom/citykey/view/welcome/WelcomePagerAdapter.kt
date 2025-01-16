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

package com.telekom.citykey.view.welcome

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomeGuideFragmentBinding
import com.telekom.citykey.models.welcome.WelcomePageItem
import com.telekom.citykey.utils.extensions.inflateChild

class WelcomePagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val pagesData: List<WelcomePageItem> = arrayListOf(
        WelcomePageItem(
            R.drawable.bg_signin_01,
            R.string.x_001_welcome_info_01,
            R.string.x_001_welcome_info_title_01
        ),
        WelcomePageItem(
            R.drawable.bg_signin_02,
            R.string.x_001_welcome_info_02,
            R.string.x_001_welcome_info_title_02
        ),
        WelcomePageItem(
            R.drawable.bg_signin_03,
            R.string.x_001_welcome_info_03,
            R.string.x_001_welcome_info_title_03
        ),
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        WelcomePageViewHolder(WelcomeGuideFragmentBinding.bind(parent.inflateChild(R.layout.welcome_guide_fragment)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WelcomePageViewHolder).setPageData(pagesData[position])
    }

    override fun getItemCount(): Int = pagesData.size

    private inner class WelcomePageViewHolder(val binding: WelcomeGuideFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setPageData(pageData: WelcomePageItem) {
            binding.welcomeImage.setImageResource(pageData.imgRes)
            binding.descHeading.setText(pageData.title)
            binding.description.setText(pageData.description)
        }
    }
}
