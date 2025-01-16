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

package com.telekom.citykey.view.services.egov.services.egovDesc

import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovDescDetailBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.loadBasicHtml
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject

class EgovDescDetail : MainFragment(R.layout.egov_desc_detail) {
    private val binding: EgovDescDetailBinding by viewBinding(EgovDescDetailBinding::bind)
    private val args: EgovDescDetailArgs by navArgs()
    private val adjustManager: AdjustManager by inject()

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarEgovDescDetail)
        binding.longDescriptionWebView.apply {
            webViewClient = pageLinkHandlerWebViewClient
            loadBasicHtml(args.egovData.longDescription)
        }
        binding.toolbarEgovDescDetail.title = args.egovData.serviceName
        binding.linksBtnList.adapter = EgovDescDetailAdapter(args.egovData.linksInfo) { service ->
            when (service.linkType) {
                EgovLinkTypes.EID_FORM, EgovLinkTypes.FORM -> {
                    adjustManager.trackEvent(R.string.open_egov_external_url)
                    findNavController()
                        .navigate(EgovDescDetailDirections.toAuthWebView2(service.link, args.egovData.serviceName))
                }

                EgovLinkTypes.PDF, EgovLinkTypes.WEB -> {
                    adjustManager.trackEvent(R.string.open_egov_external_url)
                    openLink(service.link)
                }

                else -> DialogUtil.showTechnicalError(requireContext())
            }
        }
    }
}
