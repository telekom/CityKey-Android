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

package com.telekom.citykey.view.services.service_detail_help

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.widget.TextViewCompat
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ServiceDetailHelpBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.loadBasicHtml
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ServiceDetailHelp : MainFragment(R.layout.service_detail_help) {

    private val binding: ServiceDetailHelpBinding by viewBinding(ServiceDetailHelpBinding::bind)
    private val args: ServiceDetailHelpArgs by navArgs()
    private val viewModel: ServiceDetailHelpViewModel by viewModel { parametersOf(args.service.serviceId) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.webviewToolbar)

        binding.loadingBar.setColor(CityInteractor.cityColorInt)
        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )
        binding.webviewToolbar.title = args.service.service
        binding.retryButton.setOnClickListener {
            binding.errorLayout.setVisible(false)
            binding.loadingBar.setVisible(true)
            viewModel.onRetryClicked()
        }

        subscribeUi()
    }

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
    }

    private fun subscribeUi() {
        viewModel.info.observe(viewLifecycleOwner) { info ->
            with(binding) {
                loadingBar.setVisible(false)
                if (info.isNullOrBlank()) {
                    errorLayout.setVisible(true)
                    serviceHelpWebView.setVisible(false)
                } else {
                    errorLayout.setVisible(false)
                    serviceHelpWebView.apply {
                        setVisible(true)
                        webViewClient = pageLinkHandlerWebViewClient
                        loadBasicHtml(info)
                    }
                }
            }
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                requireContext(),
                {
                    viewModel.onRetryRequired()
                    binding.loadingBar.setVisible(true)
                    binding.errorLayout.setVisible(false)
                },
                viewModel::onRetryCanceled
            )
        }
    }
}
