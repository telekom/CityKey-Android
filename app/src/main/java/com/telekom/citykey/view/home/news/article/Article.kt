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

package com.telekom.citykey.view.home.news.article

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ArticleFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.networkinterface.models.content.CityContent
import com.telekom.citykey.pictures.loadFromURLwithProgress
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.ShareUtils
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.dispatchInsetsToChildViews
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.linkifyAndLoadNonHtmlTaggedData
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.safeRun
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject

class Article : MainFragment(R.layout.article_fragment) {

    private val binding by viewBinding(ArticleFragmentBinding::bind)
    private val args: ArticleArgs by navArgs()
    private val adjustManager: AdjustManager by inject()

    private var pendingUrl: String? = null
    private var shareIntent: Intent? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupArticle(args.newsItem)
        handleWindowInsets()
        adjustManager.trackEvent(R.string.open_news_detail_page)
    }

    private fun setupArticle(cityContent: CityContent) {
        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )
        binding.btnMore.setTextColor(CityInteractor.cityColorInt)

        if (URLUtil.isValidUrl(cityContent.contentImage)) {
            binding.topLayout.visibility = View.VISIBLE
            binding.image.loadFromURLwithProgress(
                cityContent.contentImage,
                {
                    safeRun {
                        binding.loading.setVisible(false)
                        binding.errorLayout.setVisible(it)
                    }
                }
            )
        }

        binding.credits.text = cityContent.imageCredit
        pendingUrl = cityContent.contentSource

        binding.title.text = cityContent.contentTeaser
        if (!cityContent.contentSubtitle.isNullOrBlank()) {
            binding.subtitle.visibility = View.VISIBLE
            binding.subtitle.text = cityContent.contentSubtitle
        }

        if (!pendingUrl.isNullOrBlank()) {
            binding.btnMore.visibility = View.VISIBLE
            binding.btnMore.setOnClickListener {
                openLink(pendingUrl!!)
            }
        }
        when (cityContent.contentTyp) {
            "RABATTE" -> {
                binding.toolbar.title = getString(R.string.h_003_home_articles_info_discounts_headline)
                binding.headDetail.text = ""
            }

            "TIPPS" -> {
                binding.toolbar.title = getString(R.string.h_003_home_articles_info_tips_headline)
                binding.headDetail.text = getString(R.string.h_003_home_articles_info_tips_category)
            }

            "ANGEBOTE" -> {
                binding.toolbar.title = getString(R.string.h_003_home_articles_info_offers_headline)
                binding.headDetail.text = getString(R.string.h_003_home_articles_info_offers_category)
            }

            else -> {
                binding.headDetail.text = cityContent.contentCreationDate.toDateString()
                binding.headDetail.contentDescription = cityContent.contentCreationDate.toDateString().replace(".", "")
            }
        }

        cityContent.contentSource?.let {
            shareIntent = ShareUtils.createShareIntent(
                cityContent.contentTeaser ?: "",
                it,
                getString(R.string.share_store_header)
            )
        }

        binding.retryButton.setOnClickListener {
            loadImage(cityContent)
        }

        cityContent.contentDetails?.let {
            binding.contentWebView.apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        attemptOpeningWebViewUri(request?.url)
                        return true
                    }
                }
                linkifyAndLoadNonHtmlTaggedData(it)
            }
        }

        setupToolbar(binding.toolbar)
        setAccessibilityRoles()
    }

    override fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.articleAppBarLayout.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )

            insets
        }
        binding.scrollView.dispatchInsetsToChildViews(
            binding.loading,
            binding.errorLayout,
            binding.headDetail,
            binding.btnMore,
            binding.divider,
            binding.title,
            binding.subtitle,
            binding.llWebViewContainer
        ) { displayCutoutInsets ->
            binding.credits.updatePadding(
                left = displayCutoutInsets.left + 6.dpToPixel(context),
                right = displayCutoutInsets.right
            )
        }
    }

    private fun loadImage(cityContent: CityContent) {
        if (!NetworkConnection.checkInternetConnection(requireContext())) {
            DialogUtil.showRetryDialog(requireContext(), { loadImage(cityContent) })
        } else {
            binding.errorLayout.setVisible(false)
            binding.loading.setVisible(true)
            binding.image.loadFromURLwithProgress(
                cityContent.contentImage,
                {
                    safeRun {
                        binding.loading.setVisible(false)
                        binding.errorLayout.setVisible(it)
                    }
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionShare -> {
                shareIntent?.let { startActivity(it) }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAccessibilityRoles() {
        binding.title.setAccessibilityRole(AccessibilityRole.Heading, getString(R.string.accessibility_heading_level_2))
        binding.btnMore.setAccessibilityRole(AccessibilityRole.Button)
    }
}
