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

package com.telekom.citykey.view.services

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ServiceOursContainerBinding
import com.telekom.citykey.databinding.ServiceStateContainerBinding
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.services.main.ServicesViewTypes
import com.telekom.citykey.models.content.CitizenService
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.view.services.service_adapter.OurServicesAdapter

class ServicesAdapter(
    onServiceSelected: (CitizenService) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    private val ourServicesAdapter = OurServicesAdapter(onServiceSelected)
    private val viewTypes = mutableListOf<Int>()
    private var currentState: ServicesStates = ServicesStates.Loading
    private val showStateView: Boolean get() = currentState is ServicesStates.Loading || currentState is ServicesStates.Error

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == ServicesViewTypes.VIEW_TYPE_OUR_SERVICES)
            OurServicesViewHolder(ServiceOursContainerBinding.bind(parent.inflateChild(R.layout.service_ours_container)))
        else StateViewHolder(ServiceStateContainerBinding.bind(parent.inflateChild(R.layout.service_state_container)))

    override fun getItemCount() = 1 // if (showStateView) 1 else viewTypes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StateViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int) =
        if (showStateView) ServicesViewTypes.VIEW_TYPE_STATE else ServicesViewTypes.VIEW_TYPE_OUR_SERVICES

    fun setupData(state: ServicesStates) {
        currentState = state
        viewTypes.clear()
        if (showStateView) {
            notifyDataSetChanged()
            return
        }
        val data = (state as ServicesStates.Success).data
        viewTypes.addAll(data.viewTypes)
        ourServicesAdapter.updateData(data.services)
        notifyDataSetChanged()
    }

    private inner class StateViewHolder(val binding: ServiceStateContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.loadingView.setVisible(currentState == ServicesStates.Loading)
            binding.errorText.setVisible(currentState == ServicesStates.Error)
        }
    }

    private inner class OurServicesViewHolder(binding: ServiceOursContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.fewServices.apply {
                setRecycledViewPool(viewPool)
                adapter = ourServicesAdapter
            }
        }
    }
}
