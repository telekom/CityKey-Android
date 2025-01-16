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

package com.telekom.citykey.view.home.news

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.telekom.citykey.R
import com.telekom.citykey.databinding.HomeNewsFragmentBinding
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
        subscribeUi()
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
