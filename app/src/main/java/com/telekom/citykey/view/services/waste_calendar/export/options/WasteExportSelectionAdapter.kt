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

package com.telekom.citykey.view.services.waste_calendar.export.options

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WasteExportOptionsItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.networkinterface.models.waste_calendar.CalendarAccount
import com.telekom.citykey.utils.KoverIgnore
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible

class WasteExportSelectionAdapter(
    val calendarAccount: CalendarAccount,
    val resultListener: (CalendarAccount) -> Unit
) : ListAdapter<CalendarAccount, RecyclerView.ViewHolder>(diffCallback) {

    @KoverIgnore
    companion object {
        private val diffCallback = @KoverIgnore object : DiffUtil.ItemCallback<CalendarAccount>() {
            override fun areItemsTheSame(oldItem: CalendarAccount, newItem: CalendarAccount): Boolean =
                oldItem::class == newItem::class

            override fun areContentsTheSame(oldItem: CalendarAccount, newItem: CalendarAccount): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        WasteListCategoryViewHolder(WasteExportOptionsItemBinding.bind(parent.inflateChild(R.layout.waste_export_options_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WasteListCategoryViewHolder) {
            holder.bind(getItem(position))
        }
    }

    private inner class WasteListCategoryViewHolder(val binding: WasteExportOptionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarAccount) {
            binding.wasteExportOptionsItem.contentDescription =
                item.calendarDisplayName + "\n" + item.calendarAccountName
            binding.wasteExportOptionsItem.setAccessibilityRole(AccessibilityRole.Button)
            binding.name.text = item.calendarDisplayName
            binding.calendarAccount.text = item.calendarAccountName
            binding.checkedIcon.setColorFilter(CityInteractor.cityColorInt)
            (binding.sideColor.background as LayerDrawable)
                .findDrawableByLayerId(R.id.mainLayer)
                .colorFilter = PorterDuffColorFilter(item.calendarColor, PorterDuff.Mode.SRC_IN)
            binding.checkedIcon.setVisible(calendarAccount.calId == item.calId)
            binding.root.setOnClickListener {
                resultListener(item)
            }
        }
    }
}
