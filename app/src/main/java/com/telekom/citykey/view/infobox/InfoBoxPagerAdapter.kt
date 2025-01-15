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

package com.telekom.citykey.view.infobox

import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.custom.AnimateableDecoration
import com.telekom.citykey.databinding.InfoboxListBinding
import com.telekom.citykey.utils.extensions.inflateChild

class InfoBoxPagerAdapter(
    private val allMailsAdapter: InfoBoxAdapter,
    private val unreadMailsAdapter: InfoBoxAdapter
) : RecyclerView.Adapter<InfoBoxPagerAdapter.InfoBoxListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoBoxPagerAdapter.InfoBoxListViewHolder =
        InfoBoxListViewHolder(InfoboxListBinding.bind(parent.inflateChild(R.layout.infobox_list)))

    override fun onBindViewHolder(holder: InfoBoxPagerAdapter.InfoBoxListViewHolder, position: Int) {
        holder.bind(if (position == 0) unreadMailsAdapter else allMailsAdapter)
    }

    override fun getItemCount() = 2

    inner class InfoBoxListViewHolder(private val binding: InfoboxListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(adapter: InfoBoxAdapter) {
            InfoBoxSwipeCallbacks(adapter, binding.root.context).also {
                ItemTouchHelper(it).attachToRecyclerView(binding.root)
            }
            binding.root.adapter = adapter
            binding.root.addItemDecoration(
                AnimateableDecoration(
                    color = binding.root.context.getColor(R.color.separator),
                    width = 4f
                )
            )
        }
    }
}
