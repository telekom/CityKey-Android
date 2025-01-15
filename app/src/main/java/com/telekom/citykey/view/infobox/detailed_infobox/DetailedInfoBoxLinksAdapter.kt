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

package com.telekom.citykey.view.infobox.detailed_infobox

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.InfoboxDetailedLinkItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.user.InfoBoxAttachment
import com.telekom.citykey.utils.extensions.inflateChild

class DetailedInfoBoxLinksAdapter(
    private val attachments: List<InfoBoxAttachment>,
    private val clickListener: (String) -> Unit,
) : ListAdapter<String, DetailedInfoBoxLinksAdapter.LinkViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem.length == newItem.length

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder =
        LinkViewHolder(InfoboxDetailedLinkItemBinding.bind(parent.inflateChild(R.layout.infobox_detailed_link_item)))

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        val imageIcon: Drawable? =
            getDrawable(holder.binding.linkButton.context, R.drawable.ic_file_download_small)
                ?.apply {
                    setBounds(0, 0, 60, 60)
                    DrawableCompat.setTint(this, CityInteractor.cityColorInt)
                }

        holder.binding.linkButton.apply {
            val attachment = attachments[position]
            val spannableLink = SpannableString(attachment.name)
            spannableLink.setSpan(UnderlineSpan(), 0, spannableLink.length, 0)
            text = spannableLink
            setTextColor(CityInteractor.cityColorInt)
            setCompoundDrawables(imageIcon, null, null, null)
            setOnClickListener { clickListener.invoke(attachment.link) }
        }
    }

    override fun getItemCount() = attachments.size

    inner class LinkViewHolder(val binding: InfoboxDetailedLinkItemBinding) : RecyclerView.ViewHolder(binding.root)
}
