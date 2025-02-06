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

package com.telekom.citykey.view.home.news.article

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ArticleHeaderBinding
import com.telekom.citykey.databinding.ArticleHtmlBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.extensions.*

class ArticleContentAdapter(
    private val cityContent: CityContent, private val htmlContentList: List<String>, private val fragment: Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_HTML = 1
    }

    override fun getItemCount(): Int = htmlContentList.size + 1

    override fun getItemViewType(position: Int): Int = if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_HTML

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_HEADER ->
                ViewHolderHeader(ArticleHeaderBinding.bind(parent.inflateChild(R.layout.article_header)))

            else ->
                ViewHolderHtml(ArticleHtmlBinding.bind(parent.inflateChild(R.layout.article_html)))
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderHeader -> holder.loadHeaderContent()
            is ViewHolderHtml -> holder.loadHtmlContent()
        }
    }

    inner class ViewHolderHeader(val binding: ArticleHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun loadHeaderContent() {
            if (URLUtil.isValidUrl(cityContent.contentImage)) {
                binding.topLayout.visibility = View.VISIBLE
                binding.image.loadFromURLwithProgress(
                    cityContent.contentImage,
                    {
                        fragment.safeRun {
                            binding.loading.setVisible(false)
                            binding.errorLayout.setVisible(it)
                        }
                    }
                )
            }

            binding.credits.text = cityContent.imageCredit
            val pendingUrl = cityContent.contentSource

            binding.title.text = cityContent.contentTeaser
            if (!cityContent.contentSubtitle.isNullOrBlank()) {
                binding.subtitle.visibility = View.VISIBLE
                binding.subtitle.text = cityContent.contentSubtitle
            }

            with(binding.headDetail) {
                when (cityContent.contentTyp) {
                    "RABATTE" -> text = ""
                    "TIPPS" -> text = fragment.getString(R.string.h_003_home_articles_info_tips_category)
                    "ANGEBOTE" -> text = fragment.getString(R.string.h_003_home_articles_info_offers_category)
                    else -> {
                        text = cityContent.contentCreationDate.toDateString()
                        contentDescription = cityContent.contentCreationDate.toDateString().replace(".", "")
                    }
                }
            }

            with(binding.btnMore) {
                if (pendingUrl.isNullOrBlank().not()) {
                    setTextColor(CityInteractor.cityColorInt)
                    visibility = View.VISIBLE
                    setOnClickListener { fragment.openLink(pendingUrl!!) }
                }
            }

            with(binding.retryButton) {
                setTextColor(CityInteractor.cityColorInt)
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(CityInteractor.cityColorInt))
                setOnClickListener { loadImage(cityContent) }
            }
        }

        private fun loadImage(cityContent: CityContent) {
            if (!NetworkConnection.checkInternetConnection(fragment.requireContext())) {
                DialogUtil.showRetryDialog(fragment.requireContext(), { loadImage(cityContent) })
            } else {
                binding.errorLayout.setVisible(false)
                binding.loading.setVisible(true)
                binding.image.loadFromURLwithProgress(
                    cityContent.contentImage,
                    {
                        fragment.safeRun {
                            binding.loading.setVisible(false)
                            binding.errorLayout.setVisible(it)
                        }
                    }
                )
            }
        }
    }

    inner class ViewHolderHtml(val binding: ArticleHtmlBinding) : RecyclerView.ViewHolder(binding.root) {
        private val pageLinkHandlerWebViewClient by lazy {
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    fragment.attemptOpeningWebViewUri(request?.url)
                    return true
                }
            }
        }

        fun loadHtmlContent() {
            val content = htmlContentList[bindingAdapterPosition - 1]
            binding.contentWebView.apply {
                webViewClient = pageLinkHandlerWebViewClient
                linkifyAndLoadNonHtmlTaggedData(content)
            }
        }
    }

}
