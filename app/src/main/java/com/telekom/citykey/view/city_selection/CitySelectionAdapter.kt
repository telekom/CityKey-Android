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

package com.telekom.citykey.view.city_selection

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp
import com.telekom.citykey.databinding.*
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible

class CitySelectionAdapter(
    private val onFindNearestCityClicked: () -> Unit,
    private val onContactLinkClicked: () -> Unit,
    private val onItemClicked: (availableCity: AvailableCity) -> Unit
) : ListAdapter<Cities, RecyclerView.ViewHolder>(diffCallback) {

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Cities>() {
            override fun areItemsTheSame(oldItem: Cities, newItem: Cities): Boolean =
                oldItem::class == newItem::class

            override fun areContentsTheSame(oldItem: Cities, newItem: Cities): Boolean =
                oldItem == newItem
        }

        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CITY = 1
        private const val VIEW_TYPE_CITY_LOCATION_EMPTY = 2
        private const val VIEW_TYPE_CITY_LOCATION_FOUND = 3
        private const val VIEW_TYPE_CONTACT_LINK = 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_HEADER ->
                ViewHolderListHeader(CitiesHeaderItemBinding.bind(parent.inflateChild(R.layout.cities_header_item)))

            VIEW_TYPE_CITY ->
                ViewHolderCity(CitiesCityItemBinding.bind(parent.inflateChild(R.layout.cities_city_item)))

            VIEW_TYPE_CONTACT_LINK ->
                ViewHolderContactLink(CitiesContactLinkItemBinding.bind(parent.inflateChild(R.layout.cities_contact_link_item)))

            VIEW_TYPE_CITY_LOCATION_FOUND ->
                ViewHolderCityLocationFound(CitiesNearestLocationItemBinding.bind(parent.inflateChild(R.layout.cities_nearest_location_item)))

            else ->
                ViewHolderCityLocationEmpty(CitiesLocationServiceItemBinding.bind(parent.inflateChild(R.layout.cities_location_service_item)))
        }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Cities.Header -> VIEW_TYPE_HEADER
            is Cities.City -> VIEW_TYPE_CITY
            is Cities.ContactLink -> VIEW_TYPE_CONTACT_LINK
            is Cities.NearestCity -> VIEW_TYPE_CITY_LOCATION_FOUND
            else -> VIEW_TYPE_CITY_LOCATION_EMPTY
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderCity ->
                setUpCityViewHolder(holder, (getItem(position) as Cities.City).city)

            is ViewHolderCityLocationFound -> {
                setUpCityLocationFoundViewHolder(holder, (getItem(position) as Cities.NearestCity).city)
            }

            is ViewHolderCityLocationEmpty -> {
                holder.itemView.setOnClickListener {
                    onFindNearestCityClicked()
                }
                when (getItem(position)) {
                    is Cities.Progress ->
                        holder.showProgress()

                    is Cities.NoPermission ->
                        holder.showPermissionRequired()

                    else ->
                        holder.showError()
                }
            }

            is ViewHolderListHeader -> {
                holder.binding.citySelectionListHeaderTextView.apply {
                    setText((getItem(position) as Cities.Header).title)
                    setAccessibilityRole(
                        AccessibilityRole.Heading,
                        (context.getString(R.string.accessibility_heading_level_2))
                    )
                }
            }

            is ViewHolderContactLink -> {
                holder.binding.subtitleTextView.setAccessibilityRole(AccessibilityRole.Button)
                holder.itemView.setOnClickListener { onContactLinkClicked() }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun setUpCityViewHolder(holder: ViewHolderCity, city: AvailableCity) {
        holder.apply {
            setValues(city)
            if (getItem(1) is Cities.Error) {
                binding.loadCityProgress.setVisible(false)
            }

            itemView.setOnClickListener {
                onItemClicked(city)
                binding.loadCityProgress.setVisible(true)
            }

            GlideApp.with(itemView.context)
                .load(BuildConfig.IMAGE_URL + city.cityPreviewPicture)
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .into(binding.thumbnailCity)
            binding.thumbnailRing.setVisible(city.isSelected, View.INVISIBLE)

            if (city.isSelected) {
                itemView.setBackgroundColor(getColor(itemView.context, R.color.backgroundSecondary))
                itemView.isSelected = true
            } else {
                itemView.setBackgroundColor(getColor(itemView.context, R.color.background))
            }
        }
    }

    private fun setUpCityLocationFoundViewHolder(holder: ViewHolderCityLocationFound, city: AvailableCity) {
        holder.apply {
            setValues(city)

            itemView.setOnClickListener {
                onItemClicked(city)
                binding.loadNearestCityProgress.setVisible(true)
            }

            GlideApp.with(itemView.context)
                .load(BuildConfig.IMAGE_URL + city.cityPreviewPicture)
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .into(binding.thumbnailNearestCity)
        }
    }

    class ViewHolderCity(val binding: CitiesCityItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setValues(city: AvailableCity) {
            if (city.stateName.isNullOrBlank()) {
                binding.provinceTextView.setVisible(false)
                binding.root.contentDescription = city.cityName
            } else {
                binding.provinceTextView.apply {
                    setVisible(true)
                    text = city.stateName
                }
                binding.root.contentDescription = city.cityName + " " + city.stateName
            }
            binding.root.setAccessibilityRole(AccessibilityRole.Button)
            binding.cityTextView.text = city.cityName
        }
    }

    class ViewHolderContactLink(val binding: CitiesContactLinkItemBinding) : RecyclerView.ViewHolder(binding.root)

    class ViewHolderCityLocationFound(val binding: CitiesNearestLocationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setValues(city: AvailableCity) {
            if (city.stateName.isNullOrBlank()) {
                binding.provinceName.setVisible(false)
                binding.root.contentDescription = city.cityName
            } else {
                binding.provinceName.apply {
                    setVisible(true)
                    text = city.stateName
                }
                binding.root.contentDescription = city.cityName + " " + city.stateName
            }
            binding.root.setAccessibilityRole(AccessibilityRole.Button)
            binding.cityName.text = "${city.cityName} (${city.distance} km)"
        }
    }

    class ViewHolderCityLocationEmpty(val binding: CitiesLocationServiceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun showProgress() {
            binding.locationServiceIcon.setVisible(false)
            binding.cityLocationText.setVisible(false)
            binding.reloadLocationButton.setVisible(false)
            binding.loadLocationProgress.setVisible(true)
        }

        fun showPermissionRequired() {
            binding.locationServiceIcon.setVisible(true)
            binding.locationServiceIcon.loadFromDrawable(R.drawable.ic_cities_location)
            binding.cityLocationText.setVisible(true)
            binding.cityLocationText.setText(R.string.c_002_cities_turn_on_location)
            binding.cityLocationText.setAccessibilityRole(AccessibilityRole.Button)
            binding.reloadLocationButton.setVisible(false)
            binding.loadLocationProgress.setVisible(false)
        }

        fun showError() {
            binding.locationServiceIcon.setVisible(true)
            binding.locationServiceIcon.loadFromDrawable(R.drawable.ic_cities_location_fail)
            binding.cityLocationText.setVisible(true)
            binding.cityLocationText.setText(R.string.c_001_cities_location_loading_failed)
            binding.reloadLocationButton.setVisible(true)
            binding.loadLocationProgress.setVisible(false)
        }
    }

    class ViewHolderListHeader(val binding: CitiesHeaderItemBinding) : RecyclerView.ViewHolder(binding.root)
}
