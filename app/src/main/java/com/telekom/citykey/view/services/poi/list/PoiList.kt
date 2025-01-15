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

package com.telekom.citykey.view.services.poi.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiListFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.services.poi.PoiGuideViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PoiList : Fragment(R.layout.poi_list_fragment) {

    private val viewModel: PoiGuideViewModel by sharedViewModel()
    private var poiListAdapter: PoiListAdapter? = null
    private val binding by viewBinding(PoiListFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
        poiListAdapter = PoiListAdapter()
        binding.poiList.adapter = poiListAdapter
        binding.poiList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    fun subscribeUi() {
        viewModel.poiData.observe(viewLifecycleOwner) {
            poiListAdapter?.isLocationAvailable = it.isLocationAvailable
            poiListAdapter?.submitList(it.items)
        }
        viewModel.activeCategory.observe(viewLifecycleOwner) {
            poiListAdapter?.categoryName = it.categoryName
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        poiListAdapter = null
    }
}
