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

package com.telekom.citykey.view.services.egov.search

import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovSearchFullMessageBinding
import com.telekom.citykey.databinding.EgovSearchHeaderItemBinding
import com.telekom.citykey.databinding.EgovSearchHistoryItemBinding
import com.telekom.citykey.databinding.EgovServicesItemBinding
import com.telekom.citykey.domain.services.egov.EgovSearchItems
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.models.egov.EgovService
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible

class EgovServicesResultsAdapter(
    private val onServiceSelected: (EgovService) -> Unit,
    private val onHistorySelected: (String) -> Unit
) :
    ListAdapter<EgovSearchItems, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<EgovSearchItems>() {
            override fun areItemsTheSame(oldItem: EgovSearchItems, newItem: EgovSearchItems): Boolean =
                oldItem::class == newItem::class

            override fun areContentsTheSame(oldItem: EgovSearchItems, newItem: EgovSearchItems): Boolean =
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is EgovSearchItems.Header -> R.layout.egov_search_header_item
        is EgovSearchItems.History -> R.layout.egov_search_history_item
        is EgovSearchItems.FullScreenMessage -> R.layout.egov_search_full_message
        else -> R.layout.egov_services_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.egov_search_history_item ->
            EgovSearchHistoryVH(EgovSearchHistoryItemBinding.bind(parent.inflateChild(viewType)))
        R.layout.egov_search_header_item ->
            EgovSearchHeaderVH(EgovSearchHeaderItemBinding.bind(parent.inflateChild(viewType)))
        R.layout.egov_search_full_message ->
            EgovSearchFullMessageVH(EgovSearchFullMessageBinding.bind(parent.inflateChild(viewType)))
        else ->
            EgovSearchResultVH(EgovServicesItemBinding.bind(parent.inflateChild(viewType)))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is EgovSearchHeaderVH -> holder.bind((item as EgovSearchItems.Header).resId)
            is EgovSearchHistoryVH -> holder.bind((item as EgovSearchItems.History).item)
            is EgovSearchResultVH -> holder.bind((item as EgovSearchItems.Result).item)
            is EgovSearchFullMessageVH -> holder.bind(item as EgovSearchItems.FullScreenMessage)
        }
    }

    private inner class EgovSearchFullMessageVH(private val binding: EgovSearchFullMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EgovSearchItems.FullScreenMessage) {
            if (item.query == null)
                binding.message.setText(item.resId)
            else
                binding.message.text = binding.root.context.getString(item.resId, item.query)
        }
    }

    private inner class EgovSearchHeaderVH(private val binding: EgovSearchHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(@StringRes resId: Int) {
            binding.header.setAccessibilityRole(AccessibilityRole.Heading)
            binding.header.setText(resId)
        }
    }

    private inner class EgovSearchHistoryVH(private val binding: EgovSearchHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.history.setAccessibilityRole(AccessibilityRole.Button)
            binding.history.text = text

            binding.root.setOnClickListener {
                onHistorySelected(text)
            }
        }
    }

    private inner class EgovSearchResultVH(private val binding: EgovServicesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: EgovService) {
            binding.serviceTypeText.setAccessibilityRole(AccessibilityRole.Button)
            binding.serviceTitle.text = service.serviceName
            binding.serviceTypeText.text = service.linksInfo[0].title

            binding.serviceDescription.text = service.shortDescription
            binding.serviceDescription.setVisible(service.shortDescription.isNotEmpty())

            if (service.linksInfo[0].title.isEmpty()) {
                binding.serviceTypeText.setText(
                    if (service.longDescription.isEmpty()) {
                        when (service.linksInfo[0].linkType) {
                            EgovLinkTypes.WEB -> R.string.egovs_002_services_type_web
                            EgovLinkTypes.FORM -> R.string.egovs_002_services_type_form
                            EgovLinkTypes.EID_FORM -> R.string.egovs_002_services_type_form_eid
                            EgovLinkTypes.PDF -> R.string.egovs_002_services_type_pdf
                            else -> R.string.egovs_002_services_type_web
                        }
                    } else R.string.egovs_002_more_info_button

                )
            }

            binding.serviceTypeIcon.loadFromDrawable(
                if (service.longDescription.isEmpty()) {
                    when (service.linksInfo[0].linkType) {
                        EgovLinkTypes.WEB -> R.drawable.ic_egov_type_web
                        EgovLinkTypes.FORM -> R.drawable.ic_egov_type_form
                        EgovLinkTypes.EID_FORM -> R.drawable.ic_egov_type_eidform
                        EgovLinkTypes.PDF -> R.drawable.ic_egov_type_pdf
                        else -> R.drawable.ic_egov_type_web
                    }
                } else R.drawable.ic_egov_icon_content_info
            )

            binding.root.setOnClickListener {
                onServiceSelected(service)
            }
        }
    }
}
