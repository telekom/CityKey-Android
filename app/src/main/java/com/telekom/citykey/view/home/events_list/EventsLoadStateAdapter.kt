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

package com.telekom.citykey.view.home.events_list

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EventsListStateItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.city.events.NoEventsException
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setVisible

class EventsLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<EventsLoadStateAdapter.StateViewHolder>() {

    inner class StateViewHolder(val binding: EventsListStateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.retryButton.setOnClickListener { retry() }
        }

        fun setLoadState(loadState: LoadState) {
            binding.progress.setVisible(loadState is LoadState.Loading || loadState is LoadState.NotLoading)
            if (loadState is LoadState.Error) {
                binding.itemRetryLayout.setVisible(loadState.error !is NoEventsException)
                binding.noEventsMessage.setVisible(loadState.error is NoEventsException)
            } else {
                binding.itemRetryLayout.setVisible(false)
                binding.noEventsMessage.setVisible(false)
            }

            setRetryButtonTint()
        }

        private fun setRetryButtonTint() {
            binding.retryButton.setTextColor(CityInteractor.cityColorInt)
            TextViewCompat.setCompoundDrawableTintList(
                binding.retryButton,
                ColorStateList.valueOf(CityInteractor.cityColorInt)
            )
        }
    }

    override fun onBindViewHolder(holder: StateViewHolder, loadState: LoadState) {
        holder.setLoadState(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): StateViewHolder = StateViewHolder(
        EventsListStateItemBinding.bind(parent.inflateChild(R.layout.events_list_state_item))
    )
}
