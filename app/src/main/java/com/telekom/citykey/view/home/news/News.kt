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

package com.telekom.citykey.view.home.news

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.telekom.citykey.R
import com.telekom.citykey.databinding.HomeNewsFragmentBinding
import com.telekom.citykey.utils.extensions.applySafeAllInsetsWithSides
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject

class News : MainFragment(R.layout.home_news_fragment) {

    private val viewModel: NewsViewModel by inject()

    private val binding by viewBinding(HomeNewsFragmentBinding::bind)

    private var newsAdapter: NewsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsAdapter = NewsAdapter()

        with(binding.newsList) {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = newsAdapter
        }
        setupToolbar(binding.toolbarNews)
        handleWindowInsets()
        subscribeUi()
    }

    override fun handleWindowInsets() {
        binding.newsList.applySafeAllInsetsWithSides(
            left = true,
            right = true,
            bottom = true
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.newsAppBarLayout.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )

            insets
        }
    }

    private fun subscribeUi() {
        viewModel.news.observe(viewLifecycleOwner) {
            newsAdapter?.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        newsAdapter = null
    }
}
