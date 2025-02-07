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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.services.egov.details

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovServiceDetailsCategoryBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.egov.EgovGroup
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromURL
import com.telekom.citykey.utils.extensions.setAccessibilityRole

class EgovCategoriesAdapter(service: String, viewModel: EgovServiceDetailsViewModel) :
    RecyclerView.Adapter<EgovCategoriesAdapter.EgovCategoryViewHolder>() {

    private val categories = mutableListOf<EgovGroup>()
    private val service = service
    private val viewModel = viewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EgovCategoryViewHolder =
        EgovCategoryViewHolder(
            EgovServiceDetailsCategoryBinding.bind(parent.inflateChild(R.layout.egov_service_details_category))
        )

    override fun onBindViewHolder(holder: EgovCategoryViewHolder, position: Int) {
        holder.bind(categories[position], this.service)
    }

    override fun getItemCount() = categories.size

    fun submitList(list: List<EgovGroup>) {
        categories.clear()
        categories.addAll(list)

        notifyDataSetChanged()
    }

    inner class EgovCategoryViewHolder(val binding: EgovServiceDetailsCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: EgovGroup, service: String) {
            binding.root.setStrokeColor(ColorStateList.valueOf(CityInteractor.cityColorInt))
            binding.category.setAccessibilityRole(AccessibilityRole.Button)
            binding.category.text = category.groupName
            binding.icon.loadFromURL(category.groupIcon)
            binding.root.setOnClickListener {
                viewModel.clickSubCategory(service, category.groupName)
                it.findNavController().navigate(EgovServiceDetailsDirections.toEgovServices(category.groupId))
            }
        }


    }
}
