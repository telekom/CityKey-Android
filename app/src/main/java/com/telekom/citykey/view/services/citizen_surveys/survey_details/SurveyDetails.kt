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

package com.telekom.citykey.view.services.citizen_surveys.survey_details

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyDetailsFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.network.extensions.daysTotal
import com.telekom.citykey.networkinterface.models.citizen_survey.Survey
import com.telekom.citykey.pictures.loadFromOSCA
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.dispatchInsetsToChildViews
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setHtmlText
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.citizen_surveys.data_privacy.DataPrivacyNotice
import org.koin.androidx.viewmodel.ext.android.viewModel

class SurveyDetails : MainFragment(R.layout.survey_details_fragment) {

    private val viewModel: SurveyDetailsViewModel by viewModel()
    private val args: SurveyDetailsArgs by navArgs()
    private val binding by viewBinding(SurveyDetailsFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarSurveyDetails)

        binding.image.loadFromOSCA(args.serviceImage)
        binding.surveyName.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.surveyName.text = args.survey.name
        binding.contentHeader.text = args.survey.name
        binding.daysCountDown.setColor(CityInteractor.cityColorInt)
        binding.daysCountDown.setValues(args.survey.daysTotal, args.survey.daysLeft)
        args.survey.description?.let {
            binding.contentText.setHtmlText(it)
        }
        binding.daysLeftText.text = args.survey.daysLeft.toString().padStart(2, '0')

        binding.daysLeftText.contentDescription = args.survey.daysLeft.toString()
        binding.endTimeDate.text = args.survey.endDate.toDateString()
        binding.endTimeDate.contentDescription = args.survey.endDate.toDateString().replace(".", "")
        binding.creationDate.text =
            getString(R.string.cs_003_creation_date_format, args.survey.startDate.toDateString())
        binding.popularLabel.setVisible(args.survey.isPopular)
        binding.stateIcon.setVisible(args.survey.status == Survey.STATUS_COMPLETED)
        binding.stateIcon.setColorFilter(CityInteractor.cityColorInt)

        if (args.survey.daysLeft < 2) {
            binding.daysLabel.text = getString(R.string.cs_002_day_label)
        }
        binding.creationDate.contentDescription =
            getString(R.string.cs_003_creation_date_format, args.survey.startDate.toDateString().replace(".", ""))

        binding.btnStartSurvey.setupNormalStyle(CityInteractor.cityColorInt)
        if (args.survey.status == Survey.STATUS_COMPLETED) {
            binding.btnStartSurvey.visibility = View.GONE
            binding.surveyCompletedText.visibility = View.VISIBLE
            binding.surveyCompletedText.text = getString(R.string.cs_002_survey_completed_message)
        }
        binding.btnStartSurvey.setOnClickListener {
            binding.btnStartSurvey.startLoading()
            viewModel.onStartSurveyClicked(args.survey.id)
        }
        handleWindowInsets()
        subscribeUi()
    }

    override fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.toolbarSurveyDetails.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )
            insets
        }
        binding.nestedScrollView.dispatchInsetsToChildViews(
            binding.clSurveyTitle,
            binding.clSurveyDuration,
            binding.contentHeader,
            binding.contentText,
            binding.btnStartSurvey,
            binding.surveyCompletedText
        )
    }

    private fun subscribeUi() {
        viewModel.surveyAvailable.observe(viewLifecycleOwner) {
            binding.btnStartSurvey.stopLoading()
            findNavController().navigate(
                SurveyDetailsDirections.actionSurveyDetailsToSurveyQuestions(
                    args.survey.id,
                    args.survey.name
                )
            )
        }
        viewModel.surveyDataPrivacy.observe(viewLifecycleOwner) {
            if (viewModel.surveyDataPrivacyAccepted.value == false) {
                binding.btnStartSurvey.stopLoading()
                DataPrivacyNotice(it.surveyDataPrivacyText, CityInteractor.cityColorInt) {
                    if (it) {
                        viewModel.onDataPrivacyAccepted(args.survey.id)
                        binding.btnStartSurvey.startLoading()
                        viewModel.onStartSurveyClicked(args.survey.id)
                    }
                }.showDialog(childFragmentManager)
            }
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.btnStartSurvey.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                binding.btnStartSurvey.stopLoading()
                viewModel.onRetryCanceled()
            }
        }
    }
}
