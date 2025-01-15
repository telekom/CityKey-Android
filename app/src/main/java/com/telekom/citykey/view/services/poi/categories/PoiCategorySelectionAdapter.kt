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

package com.telekom.citykey.view.services.poi.categories

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiCategoryGroupItemBinding
import com.telekom.citykey.databinding.PoiCategoryItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.poi.PoiCategory
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible

class PoiCategorySelectionAdapter(
    private val clickListener: (category: PoiCategory) -> Unit,
    private val category: PoiCategory?,
) : ListAdapter<PoiCategoryListItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PoiCategoryListItem>() {
            override fun areItemsTheSame(oldItem: PoiCategoryListItem, newItem: PoiCategoryListItem): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: PoiCategoryListItem, newItem: PoiCategoryListItem): Boolean =
                oldItem == newItem
        }
    }

    private var holder: ItemViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.poi_category_group_item ->
            HeaderViewHolder(PoiCategoryGroupItemBinding.bind(parent.inflateChild(viewType)))

        else ->
            ItemViewHolder(PoiCategoryItemBinding.bind(parent.inflateChild(viewType)))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PoiCategoryListItem.Header -> R.layout.poi_category_group_item
        is PoiCategoryListItem.Item -> R.layout.poi_category_item
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.setup(getItem(position))
            is ItemViewHolder -> holder.setup(getItem(position))
        }
    }

    override fun submitList(list: List<PoiCategoryListItem>?) {
        super.submitList(list)
        holder = null
    }

    private inner class ItemViewHolder(val binding: PoiCategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var loadingCategory: PoiCategoryListItem.Item? = null
        fun setup(item: PoiCategoryListItem) {
            if (item is PoiCategoryListItem.Item) {
                binding.categoryName.setAccessibilityRole(AccessibilityRole.Button)
                binding.categorySelectedIcon.setColorFilter(CityInteractor.cityColorInt)
                binding.categoryName.text = item.categoryListItem.categoryName
                if (item.categoryListItem.categoryId != loadingCategory?.categoryListItem?.categoryId)
                    binding.categoryProgress.setVisible(false)
                itemView.setOnClickListener {
                    holder = this
                    item.categoryListItem.categoryGroupId = item.categoryGroupId
                    item.categoryListItem.categoryIcon = item.categoryGroupIcon
                    clickListener(item.categoryListItem)
                    binding.categoryProgress.setVisible(true)
                    loadingCategory = item
                }

                if (category?.categoryGroupId == item.categoryGroupId && category.categoryId == item.categoryListItem.categoryId) {
                    binding.categorySelectedIcon.visibility = View.VISIBLE
                    binding.root.isSelected = true
                } else {
                    binding.categorySelectedIcon.visibility = View.INVISIBLE
                    binding.root.isSelected = false
                }
            }
        }
    }

    fun stopLoading() {
        (holder)?.binding?.categoryProgress?.setVisible(false)
        holder = null
    }

    private inner class HeaderViewHolder(private val binding: PoiCategoryGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setup(item: PoiCategoryListItem) {
            if (item is PoiCategoryListItem.Header) {
                binding.categoryGroupName.text = item.categoryGroup.categoryGroupName
                binding.categoryGroupName.apply {
                    setAccessibilityRole(
                        AccessibilityRole.Heading,
                        context.getString(R.string.accessibility_heading_level_2)
                    )
                }
                binding.categoryGroupIcon.loadFromDrawable(item.categoryGroup.categoryGroupIconId)
                binding.categoryGroupIcon.setColorFilter(CityInteractor.cityColorInt)
            }
        }
    }
}
