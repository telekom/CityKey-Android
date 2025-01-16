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

package com.telekom.citykey.view.services.detailed_service

import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp
import com.telekom.citykey.databinding.ServiceDetailsFragmentBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.track.AnalyticsParameterKey
import com.telekom.citykey.models.content.ServiceAction
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.loadBasicHtml
import com.telekom.citykey.utils.extensions.openApp
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.services.DetailedServiceButtonsAdapter
import com.telekom.citykey.view.services.ServicesFunctions
import com.telekom.citykey.view.services.citizen_surveys.SurveysOverviewViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailedService : MainFragment(R.layout.service_details_fragment) {
    private val args: DetailedServiceArgs by navArgs()
    private val binding by viewBinding(ServiceDetailsFragmentBinding::bind)
    private val adjustManager: AdjustManager by inject()
    private val surveysViewModel: SurveysOverviewViewModel by viewModel()

    companion object {
        private const val ACTION_OPEN_APP = 2
        private const val ACTION_OPEN_LINK = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarDetailedServices.title = args.service.service
        setupToolbar(binding.toolbarDetailedServices)

        setupViews()

        with(requireActivity() as MainActivity) {
            if (currentDeeplinkString == getString(R.string.deeplink_polls_list)) {
                binding.actionsList.post {
                    binding.actionsList.layoutManager?.findViewByPosition(0)?.performClick()
                }
            }
        }
    }

    private fun setupViews() {
        val imageUrl = BuildConfig.IMAGE_URL +
                if (args.service.headerImage.isNullOrBlank().not()) args.service.headerImage else args.service.image
        GlideApp.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(binding.image)

        if (!args.service.helpLinkTitle.isNullOrBlank()) {
            binding.helpActionContainer.apply {
                setVisible(true)
                binding.helpActionLabel.text = args.service.helpLinkTitle
                setOnClickListener {
                    findNavController().navigate(
                        DetailedServiceDirections.actionDetailedServiceToServiceHelp(args.service)
                    )
                }
            }
        }
        args.service.serviceAction?.let { actions ->
            binding.actionsList.adapter = DetailedServiceButtonsAdapter(actions) {
                adjustManager.trackEvent(
                    R.string.web_tile_button_tapped,
                    mapOf(
                        AnalyticsParameterKey.serviceType to (args.service.serviceType ?: "").toString(),
                        AnalyticsParameterKey.serviceActionType to (it.actionType ?: "").toString(),
                    )
                )
                when (args.service.function) {
                    ServicesFunctions.URKUNDE, ServicesFunctions.MARKT_STAND, ServicesFunctions.EGOV_LIGHT -> findNavController().navigate(
                        DetailedServiceDirections.actionDetailedServiceToAuthWebView2(
                            it.androidUri,
                            args.service.service
                        )
                    )

                    ServicesFunctions.TOURISM -> findNavController().navigate(
                        DetailedServiceDirections.actionDetailedServiceToAuthWebView2(
                            it.androidUri,
                            it.visibleText
                        )
                    )

                    ServicesFunctions.SURVEYS -> {
                        adjustManager.trackEvent(R.string.open_service_survey_list)
                        if (surveysViewModel.shouldNavigateToSurveyDetails()) {
                            findNavController().navigate(
                                DetailedServiceDirections.actionDetailedServiceToSurveysOverview(
                                    args.service.image
                                )
                            )
                        }
                    }

                    ServicesFunctions.MOBILITY -> {
                        if (URLUtil.isValidUrl(it.androidUri)) {
                            findNavController().navigate(
                                DetailedServiceDirections.actionDetailedServiceToWebview(
                                    it.androidUri,
                                    args.service.service,
                                    args.service.serviceParams?.get("User"),
                                    args.service.serviceParams?.get("Password")
                                )
                            )
                        } else {
                            DialogUtil.showTechnicalError(requireContext())
                        }
                    }

                    else -> {
                        openByAction(it)
                    }
                }
            }
        }
        binding.serviceContentWebView.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    attemptOpeningWebViewUri(request?.url)
                    return true
                }
            }
            loadBasicHtml(args.service.description)
        }
    }

    private fun openByAction(serviceAction: ServiceAction) {
        when (serviceAction.action) {
            ACTION_OPEN_APP -> {
                try {
                    openApp(serviceAction.androidUri)
                } catch (exception: Exception) {
                    DialogUtil.showTechnicalError(requireContext())
                }
            }

            ACTION_OPEN_LINK -> {
                if (URLUtil.isValidUrl(serviceAction.androidUri)) {
                    openLink(serviceAction.androidUri)
                } else {
                    DialogUtil.showTechnicalError(requireContext())
                }
            }
        }
    }
}
