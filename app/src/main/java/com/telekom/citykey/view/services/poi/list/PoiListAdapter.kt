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

package com.telekom.citykey.view.services.poi.list

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiListItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.network.extensions.categoryGroupIconId
import com.telekom.citykey.networkinterface.models.poi.PointOfInterest
import com.telekom.citykey.pictures.loadFromDrawable
import com.telekom.citykey.utils.KoverIgnore
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.decodeHTML
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.view.services.poi.PoiGuideDirections

class PoiListAdapter : ListAdapter<PointOfInterest, RecyclerView.ViewHolder>(diffCallback) {

    @KoverIgnore
    companion object {

        private val diffCallback = @KoverIgnore object : DiffUtil.ItemCallback<PointOfInterest>() {
            override fun areItemsTheSame(oldItem: PointOfInterest, newItem: PointOfInterest): Boolean =
                oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: PointOfInterest, newItem: PointOfInterest): Boolean =
                oldItem == newItem
        }
    }

    var isLocationAvailable: Boolean = false
    var categoryName: String = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PoiListViewHolder(PoiListItemBinding.bind(parent.inflateChild(R.layout.poi_list_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PoiListViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    private inner class PoiListViewHolder(val binding: PoiListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: PointOfInterest, position: Int) {
            binding.categoryGroupName.apply {
                this.contentDescription = context.getString(
                    R.string.a11y_list_item_position,
                    position + 1,
                    itemCount
                ) + item.title
            }
            binding.root.setAccessibilityRole(AccessibilityRole.Button)
            binding.addressLabel.setVisible(item.address.isNotEmpty())
            binding.address.setVisible(item.address.isNotEmpty())
            binding.openingHours.setVisible(item.openHours.isNotEmpty())
            binding.openingHours.setLinkTextColor(CityInteractor.cityColorInt)
            binding.openingHoursLabel.setVisible(item.openHours.isNotEmpty())
            binding.distance.setVisible(isLocationAvailable)
            binding.address.text = item.address
            binding.categoryGroupName.text = item.title
            binding.openingHours.text = item.openHours.decodeHTML()
            binding.distance.text = "${item.distance} km"
            binding.categoryGroupIcon.loadFromDrawable(item.categoryGroupIconId)
            binding.categoryGroupIcon.setColorFilter(CityInteractor.cityColorInt)

            binding.root.setOnClickListener {
                it.findNavController()
                    .navigate(PoiGuideDirections.actionPointsOfInterestToPoiGuideDetails(item, categoryName))
            }
        }
    }
}
