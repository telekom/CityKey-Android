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

package com.telekom.citykey.view.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DataPrivacyNoticeDialogBinding
import com.telekom.citykey.domain.legal_data.LegalDataManager
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.android.ext.android.inject

class DataPrivacyNoticeDialog(private val isLaunchedFromSettings: Boolean = false) :
    FullScreenBottomSheetDialogFragment(R.layout.data_privacy_notice_dialog) {

    private val binding: DataPrivacyNoticeDialogBinding by viewBinding(DataPrivacyNoticeDialogBinding::bind)
    private val legalData: LegalDataManager by inject()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOpenSettings.setOnClickListener {
            if (isLaunchedFromSettings) dismiss()
            else DataPrivacySettingsDialog(isLaunchedFromNotice = true)
                .showDialog(parentFragmentManager, "DataPrivacySettingsDialog")
        }

        binding.version.text = "App-Version: ${BuildConfig.VERSION_NAME}"

        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        subscribeUi()
    }

    private fun subscribeUi() {
        legalData.legalInfo.observe(viewLifecycleOwner) { terms ->
            binding.dataPrivacyText1.apply {
                webViewClient = pageLinkHandlerWebViewClient
                setBackgroundColor(Color.TRANSPARENT)
                linkifyAndLoadNonHtmlTaggedData(terms.dataSecurity.dataUsage)
            }
            binding.dataPrivacyText2.apply {
                webViewClient = pageLinkHandlerWebViewClient
                setBackgroundColor(Color.TRANSPARENT)
                linkifyAndLoadNonHtmlTaggedData(terms.dataSecurity.dataUsage2)
            }
        }
    }

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
    }

}
