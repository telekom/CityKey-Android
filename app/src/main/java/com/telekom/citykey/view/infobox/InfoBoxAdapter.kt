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

package com.telekom.citykey.view.infobox

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.InfoboxEmptyItemBinding
import com.telekom.citykey.databinding.InfoboxListItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.user.InfoBoxContent
import com.telekom.citykey.utils.diffutil_callbacks.InfoBoxDiffUtils
import com.telekom.citykey.utils.extensions.*
import java.util.*

class InfoBoxAdapter(
    private val toggleReadListener: (read: Boolean, id: Int) -> Unit,
    private val deleteListener: (id: InfoBoxContent) -> Unit,
    @StringRes private val emptyMsgId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<InfoboxItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == R.layout.infobox_list_item)
            InfoBoxHolder(InfoboxListItemBinding.bind(parent.inflateChild(R.layout.infobox_list_item)))
        else
            InfoBoxEmptyHolder(InfoboxEmptyItemBinding.bind(parent.inflateChild(R.layout.infobox_empty_item)))

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int =
        if (items[position] is InfoboxItem.Empty) R.layout.infobox_empty_item else R.layout.infobox_list_item

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is InfoBoxHolder -> holder.bind((items[position] as InfoboxItem.Mail).item)
            is InfoBoxEmptyHolder -> holder.bind()
        }
    }

    fun updateData(data: List<InfoboxItem>) {
        val oldList = items.toList()
        items.clear()
        items.addAll(data)

        DiffUtil.calculateDiff(InfoBoxDiffUtils(oldList, items))
            .dispatchUpdatesTo(this)
    }

    fun deleteItem(position: Int) {
        deleteListener((items[position] as InfoboxItem.Mail).item)
    }

    fun getItem(position: Int) = (items[position] as InfoboxItem.Mail).item

    fun toggleItemRead(position: Int) {
        val item = (items[position] as InfoboxItem.Mail).item
        toggleReadListener(item.isRead, item.userInfoId)
    }

    inner class InfoBoxEmptyHolder(private val binding: InfoboxEmptyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.imgInfoBoxEmptyMessage.setText(emptyMsgId)
        }
    }

    private inner class InfoBoxHolder(private val binding: InfoboxListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: InfoBoxContent) {
            binding.layoutItemInfoBox.setAccessibilityRole(AccessibilityRole.Button)
            binding.icon.loadFromOSCA(item.category.icon)
            binding.title.text = item.headline
            binding.description.text = item.description
            binding.category.text = item.category.name
            binding.status.setBackgroundColor(CityInteractor.cityColorInt)
            binding.status.setVisible(!item.isRead, View.INVISIBLE)

            if (item.creationDate.isToday) {
                binding.date.text = item.creationDate.getHoursAndMins()
                    .format(item.creationDate)
            } else {
                val dateAsCalendar = item.creationDate.toCalendar()
                binding.date.text =
                    "${dateAsCalendar.getShortMonthName()} ${dateAsCalendar.get(Calendar.DAY_OF_MONTH)}"
            }

            binding.attachments.setColorFilter(CityInteractor.cityColorInt)
            binding.attachments.setVisible(!item.attachments.isNullOrEmpty())

            binding.root.setOnClickListener {
                if (!item.isRead) {
                    item.isRead = true
                    toggleReadListener(false, item.userInfoId)
                }
                it.findNavController()
                    .navigate(InfoBoxDirections.actionInfoBoxToDetailedInfoBox(item))
            }
        }
    }
}
