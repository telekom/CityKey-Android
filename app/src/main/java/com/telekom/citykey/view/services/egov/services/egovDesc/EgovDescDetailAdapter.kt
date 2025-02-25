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

package com.telekom.citykey.view.services.egov.services.egovDesc

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovLinksItemBinding
import com.telekom.citykey.models.egov.EgovLinkInfo
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.setAccessibilityRole

class EgovDescDetailAdapter(
    private val linksInfo: List<EgovLinkInfo>,
    private val clickListener: (EgovLinkInfo) -> Unit
) : RecyclerView.Adapter<EgovDescDetailAdapter.LinkViewButtonHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewButtonHolder =
        LinkViewButtonHolder(EgovLinksItemBinding.bind(parent.inflateChild(R.layout.egov_links_item)))

    override fun onBindViewHolder(holder: LinkViewButtonHolder, position: Int) {
        holder.bind(linksInfo[position])
    }

    override fun getItemCount() = linksInfo.size

    inner class LinkViewButtonHolder(private val binding: EgovLinksItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemInfo: EgovLinkInfo) {
            binding.serviceTypeText.text = itemInfo.title
            binding.serviceTypeText.setAccessibilityRole(AccessibilityRole.Button)
            if (itemInfo.title.isEmpty()) {
                binding.serviceTypeText.setText(
                    when (itemInfo.linkType) {
                        EgovLinkTypes.WEB -> R.string.egovs_002_services_type_web
                        EgovLinkTypes.FORM -> R.string.egovs_002_services_type_form
                        EgovLinkTypes.EID_FORM -> R.string.egovs_002_services_type_form_eid
                        EgovLinkTypes.PDF -> R.string.egovs_002_services_type_pdf
                        else -> R.string.egovs_002_services_type_web
                    }
                )
            }

            binding.serviceTypeIcon.loadFromDrawable(
                when (itemInfo.linkType) {
                    EgovLinkTypes.WEB -> R.drawable.ic_egov_type_web
                    EgovLinkTypes.FORM -> R.drawable.ic_egov_type_form
                    EgovLinkTypes.EID_FORM -> R.drawable.ic_egov_type_eidform
                    EgovLinkTypes.PDF -> R.drawable.ic_egov_type_pdf
                    else -> R.drawable.ic_egov_type_web
                }
            )
            binding.root.setOnClickListener {
                clickListener(itemInfo)
            }
        }
    }
}
