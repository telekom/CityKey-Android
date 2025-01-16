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

package com.telekom.citykey.view.home.feed_adapters

import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.HomeNewsEmptyItemBinding
import com.telekom.citykey.databinding.HomeNewsEvenItemBinding
import com.telekom.citykey.databinding.HomeNewsHeaderItemBinding
import com.telekom.citykey.databinding.HomeNewsOddItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromURL
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.view.home.HomeDirections
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NewsFeedAdapter : ListAdapter<CityContent, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private const val VIEW_TYPE_EVEN = 0
        private const val VIEW_TYPE_ODD = 1
        private const val VIEW_TYPE_EMPTY = 2
        private const val VIEW_TYPE_HEADER = 3

        private val diffCallback = object : DiffUtil.ItemCallback<CityContent>() {
            override fun areItemsTheSame(oldItem: CityContent, newItem: CityContent) =
                oldItem.contentId == newItem.contentId

            override fun areContentsTheSame(oldItem: CityContent, newItem: CityContent) =
                oldItem == newItem
        }
    }

    private var showStateView = true
    private var state: NewsState = NewsState.Loading

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_ODD -> OddViewHolder(HomeNewsOddItemBinding.bind(parent.inflateChild(R.layout.home_news_odd_item)))
            VIEW_TYPE_EVEN -> EvenViewHolder(HomeNewsEvenItemBinding.bind(parent.inflateChild(R.layout.home_news_even_item)))
            VIEW_TYPE_HEADER -> HeaderViewHolder(HomeNewsHeaderItemBinding.bind(parent.inflateChild(R.layout.home_news_header_item)))
            else -> StateViewHolder(HomeNewsEmptyItemBinding.bind(parent.inflateChild(R.layout.home_news_empty_item)))
        }

    override fun getItemCount() = if (showStateView) 2 else super.getItemCount() + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewsViewHolder -> {
                val item = getItem(position - 1)
                holder.bind(item, currentList.size)

                holder.itemView.apply {
                    setOnClickListener {
                        it.findNavController().navigate(HomeDirections.actionHomeToArticle(item))
                    }
                }
            }

            is HeaderViewHolder -> {
                holder.bind(super.getItemCount() > 0)
            }

            is StateViewHolder -> {
                holder.bind()
            }
        }
    }

    fun updateNews(newsState: NewsState) {
        val prevState = state
        state = newsState
        when (newsState) {
            is NewsState.Loading, is NewsState.Error, is NewsState.ActionError -> {
                showStateView = true
                submitList(emptyList())
                if (prevState == NewsState.Loading || prevState == NewsState.Error ||
                    prevState == NewsState.ActionError
                ) {
                    notifyItemChanged(itemCount - 1)
                } else {
                    notifyDataSetChanged()
                }
            }

            is NewsState.Success -> {
                if (newsState.content.isEmpty()) {
                    showStateView = true
                    if (prevState == NewsState.Loading || prevState == NewsState.Error) {
                        notifyItemChanged(itemCount - 1)
                    } else {
                        val prevStateNewsCount = (prevState as? NewsState.Success)?.content?.size ?: 0
                        notifyItemRangeRemoved(0, prevStateNewsCount)
                        notifyItemInserted(itemCount)
                    }
                    submitList(emptyList())
                } else {
                    showStateView = false
                    notifyItemRemoved(itemCount - 1)
                    submitList(newsState.content)
                }
            }
        }
    }

    override fun getItemViewType(position: Int) = when {
        position == 0 -> VIEW_TYPE_HEADER
        showStateView && position == 1 -> VIEW_TYPE_EMPTY
        position % 2 == 0 -> VIEW_TYPE_EVEN
        else -> VIEW_TYPE_ODD
    }

    override fun submitList(list: List<CityContent>?) {
        super.submitList(list)
        notifyItemChanged(0)
    }

    abstract class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(newsItem: CityContent, size: Int)
    }

    class EvenViewHolder(private val binding: HomeNewsEvenItemBinding) :
        NewsViewHolder(binding.root) {
        override fun bind(newsItem: CityContent, size: Int) {
            binding.evenTime.text = newsItem.contentCreationDate.toDateString()
            binding.evenTime.contentDescription = newsItem.contentCreationDate.toDateString().replace(".", "")
            binding.evenDescription.text = newsItem.contentTeaser
            binding.evenImage.loadFromURL(newsItem.thumbnail)
            binding.layoutItemHomeNewsEven.apply {
                contentDescription = context.getString(
                    R.string.a11y_list_item_position,
                    position,
                    size
                ) + newsItem.contentTeaser + newsItem.contentCreationDate.toDateString().replace(".", "")
                setAccessibilityRole(AccessibilityRole.Link)
            }
        }
    }

    class OddViewHolder(private val binding: HomeNewsOddItemBinding) :
        NewsViewHolder(binding.root) {
        override fun bind(newsItem: CityContent, size: Int) {
            binding.oddTime.text = newsItem.contentCreationDate.toDateString()
            binding.oddTime.contentDescription = newsItem.contentCreationDate.toDateString().replace(".", "")
            binding.oddDescription.text = newsItem.contentTeaser
            binding.oddImage.loadFromURL(newsItem.thumbnail)
            binding.layoutItemHomeNewsOdd.apply {
                contentDescription = context.getString(
                    R.string.a11y_list_item_position,
                    position,
                    size
                ) + newsItem.contentTeaser + newsItem.contentCreationDate.toDateString().replace(".", "")
                setAccessibilityRole(AccessibilityRole.Link)
            }
        }
    }

    private inner class StateViewHolder(private val binding: HomeNewsEmptyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            when (state) {
                is NewsState.Success -> {
                    binding.progress.setVisible(false)
                    binding.errorText.setText(R.string.h_001_home_no_news)
                    binding.errorText.setVisible(true)
                }

                is NewsState.Error -> {
                    binding.progress.setVisible(false)

                    binding.errorText.setText(R.string.h_001_home_news_error)
                    binding.errorText.setVisible(true)
                }

                is NewsState.Loading -> {
                    binding.errorText.setVisible(false)
                    binding.progress.setVisible(true)
                }

                is NewsState.ActionError -> {
                    binding.progress.setVisible(false)
                    binding.errorText.setText(R.string.h_001_home_news_action_error)
                    binding.errorText.setVisible(true)
                }
            }
        }
    }

    private inner class HeaderViewHolder(private val binding: HomeNewsHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root), KoinComponent {
        private val adjustManager: AdjustManager by inject()

        init {
            binding.labelCurNews.apply {
                setAccessibilityRole(
                    AccessibilityRole.Heading,
                    (context.getString(R.string.accessibility_heading_level_2))
                )
            }
            binding.btnShowMore.apply {
                setAccessibilityRole(AccessibilityRole.Button)
                setOnClickListener {
                    adjustManager.trackEvent(R.string.open_news_list)
                    it.findNavController().navigate(HomeDirections.actionHomeToNews())
                }
            }
        }

        fun bind(isShowMoreVisible: Boolean) {
            binding.root.setBackgroundColor(CityInteractor.cityColorInt)
            binding.btnShowMore.setVisible(isShowMoreVisible)
        }
    }
}
