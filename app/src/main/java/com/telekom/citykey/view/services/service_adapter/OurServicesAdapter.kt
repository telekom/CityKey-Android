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

package com.telekom.citykey.view.services.service_adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ServiceOursItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.content.CitizenService
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromOSCA
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible

class OurServicesAdapter(
    val onServiceSelected: (CitizenService) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_OUR_SERVICE = 52
        private const val VIEW_TYPE_TITLE = 102
    }

    private val items = mutableListOf<CitizenService>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == VIEW_TYPE_OUR_SERVICE)
            ServiceViewHolder(ServiceOursItemBinding.bind(parent.inflateChild(R.layout.service_ours_item)))
        else
            TitleViewHolder(parent.inflateChild(R.layout.service_ours_title))

    override fun getItemCount() = items.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ServiceViewHolder) {
            holder.bind(items[position - 1])
        }
    }

    fun updateData(newItems: List<CitizenService>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) =
        if (position == 0) VIEW_TYPE_TITLE else VIEW_TYPE_OUR_SERVICE

    inner class ServiceViewHolder(val binding: ServiceOursItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CitizenService) {
            binding.title.text = item.service
            binding.image.loadFromOSCA(item.image)
            binding.icon.loadFromOSCA(item.icon)
            binding.ribbon.setColorFilter(CityInteractor.cityColorInt)
            binding.ribbonBox.setVisible(item.isNew)

            itemView.tag = item.function
            binding.badgeText.setVisible(binding.badgeText.isVisible && itemView.tag != null)

            when {
                item.loginLocked -> showLoginLock()
                item.cityLocked != null -> showCityLock()
                else -> hideLocks()
            }

            itemView.setOnClickListener { onServiceSelected(item) }
            binding.imageBox.apply {
                contentDescription = item.service
                setAccessibilityRole(AccessibilityRole.Button)
            }
        }

        private fun showLoginLock() {
            binding.restrictionIndicator.setImageResource(R.drawable.ic_icon_locked_content)
            binding.restrictionIndicator.visibility = View.VISIBLE
        }

        private fun showCityLock() {
            binding.restrictionIndicator.setImageResource(R.drawable.ic_limited_content)
            binding.restrictionIndicator.visibility = View.VISIBLE
        }

        private fun hideLocks() {
            binding.restrictionIndicator.visibility = View.GONE
        }
    }

    inner class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<TextView>(R.id.ourTitle).apply {
                setAccessibilityRole(
                    AccessibilityRole.Heading,
                    context.getString(R.string.accessibility_heading_level_2)
                )
            }
        }
    }

}
