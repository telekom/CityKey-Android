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

package com.telekom.citykey.view.services.citizen_surveys

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyOverviewFragmentBinding
import com.telekom.citykey.domain.services.surveys.SurveysState
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.isNotVisible
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SurveysOverview : MainFragment(R.layout.survey_overview_fragment) {

    private val viewModel: SurveysOverviewViewModel by viewModel()
    private val args: SurveysOverviewArgs by navArgs()
    private val binding by viewBinding(SurveyOverviewFragmentBinding::bind)

    private var listAdapter: SurveysAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarSurveys)
        listAdapter = SurveysAdapter(viewModel.isPreview()) {
            findNavController().navigate(
                SurveysOverviewDirections.actionSurveysOverviewToSurveyDetails(it, args.serviceImage)
            )
        }

        binding.surveysList.adapter = listAdapter
        binding.surveysList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.onRefreshRequested()
        }
        binding.retryButton.setAccessibilityRole(AccessibilityRole.Button)
        binding.retryButton.setOnClickListener {
            viewModel.onRefreshRequested()
        }
        binding.textEmptyState.apply {
            if (viewModel.isPreview()) {
                text = context.getString(R.string.cs_002_preview_error_no_surveys)
            } else {
                text = context.getString(R.string.cs_002_error_no_surveys)
            }
        }
        subscribeUi()
        with(requireActivity() as MainActivity) {
            markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_polls_list))
        }
    }

    private fun subscribeUi() {
        viewModel.listItems.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.loader.setVisible(false)
            listAdapter?.submitList(it)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            with(binding) {
                when (it) {
                    SurveysState.ServiceNotAvailable -> {
                        swipeRefreshLayout.isRefreshing = false
                        findNavController().popBackStack(R.id.services, false)
                    }

                    SurveysState.Loading -> {
                        val shouldShowLoader =
                            surveysList.isNotVisible && textEmptyState.isNotVisible && swipeRefreshLayout.isRefreshing.not()
                        loader.setVisible(shouldShowLoader)
                    }

                    else -> {
                        swipeRefreshLayout.isRefreshing = false
                        loader.setVisible(false)
                        errorLayout.setVisible(it == SurveysState.Error)
                        textEmptyState.setVisible(it == SurveysState.Empty)
                        surveysList.setVisible(it == SurveysState.Success)
                    }
                }
            }
        }

        viewModel.stopRefresh.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.loader.setVisible(false)
        }
    }

    override fun onDestroyView() {
        listAdapter = null
        super.onDestroyView()
    }
}
