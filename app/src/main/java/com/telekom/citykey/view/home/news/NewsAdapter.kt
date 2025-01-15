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

import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.HomeNewsItemBinding
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromURL
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.toDateString

class NewsAdapter : ListAdapter<CityContent, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<CityContent>() {
            override fun areItemsTheSame(oldItem: CityContent, newItem: CityContent): Boolean =
                oldItem.contentId == newItem.contentId

            override fun areContentsTheSame(oldItem: CityContent, newItem: CityContent): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        NewsViewHolder(HomeNewsItemBinding.bind(parent.inflateChild(R.layout.home_news_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsViewHolder) {
            holder.bind(getItem(position))
            holder.binding.layoutItemNews.apply {
                contentDescription = context.getString(
                    R.string.a11y_list_item_position,
                    position + 1,
                    itemCount
                ) + "\n" + getItem(position).contentTeaser + "\n" + getItem(position).contentCreationDate.toDateString()
                    .replace(".", "")
                setAccessibilityRole(AccessibilityRole.Link)
            }
        }
    }

    private class NewsViewHolder(val binding: HomeNewsItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(content: CityContent) {
            binding.timeStamp.text = content.contentCreationDate.toDateString()
            binding.timeStamp.contentDescription = content.contentCreationDate.toDateString().replace(".", "")
            binding.description.text = content.contentTeaser
            binding.image.loadFromURL(content.thumbnail)

            binding.root.setOnClickListener {
                it.findNavController()
                    .navigate(
                        NewsDirections.actionNewsToArticle(content)
                    )
            }
        }
    }
}
