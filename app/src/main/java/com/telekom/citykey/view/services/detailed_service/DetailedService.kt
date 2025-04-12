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

package com.telekom.citykey.view.services.detailed_service

import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ServiceDetailsFragmentBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.track.AnalyticsParameterKey
import com.telekom.citykey.networkinterface.models.content.ServiceAction
import com.telekom.citykey.pictures.loadCenterCropped
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.dispatchInsetsToChildViews
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.linkifyAndLoadNonHtmlTaggedData
import com.telekom.citykey.utils.extensions.openApp
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
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
        handleWindowInsets()

        with(requireActivity() as MainActivity) {
            if (currentDeeplinkString == getString(R.string.deeplink_polls_list)) {
                binding.actionsList.post {
                    binding.actionsList.layoutManager?.findViewByPosition(0)?.performClick()
                }
            }
        }
    }

    private fun setupViews() {
        binding.image.loadCenterCropped(
            imageReference = if (args.service.headerImage.isNullOrBlank().not()) {
                args.service.headerImage
            } else {
                args.service.image
            }
        )
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
            linkifyAndLoadNonHtmlTaggedData(args.service.description)
        }
    }

    override fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.serviceDetailsABL.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )

            insets
        }
        binding.scrollView.dispatchInsetsToChildViews(
            binding.llcServiceContent,
            binding.actionsList
        ) { displayCutoutInsets ->
            binding.helpActionContainer.updatePadding(
                left = displayCutoutInsets.left + 21.dpToPixel(context),
                right = displayCutoutInsets.right + 21.dpToPixel(context)
            )
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
