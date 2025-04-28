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

package com.telekom.citykey.view.services.defect_reporter.details

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectReporterServiceDetailFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.pictures.loadCenterCropped
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.dispatchInsetsToChildViews
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class DefectServiceDetail : MainFragment(R.layout.defect_reporter_service_detail_fragment) {

    private val viewModel: DefectServiceDetailViewModel by viewModel()
    private val binding by viewBinding(DefectReporterServiceDetailFragmentBinding::bind)
    private val args: DefectServiceDetailArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        handleWindowInsets()
        subscribeUi()
        (activity as? MainActivity)?.markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_defect_reporter))
    }

    private fun setUpViews() {

        binding.toolbarDefectReporter.title = args.service.service
        setupToolbar(binding.toolbarDefectReporter)

        binding.reportDefectBtn.setupNormalStyle(CityInteractor.cityColorInt)

        // https://jira.telekom.de/browse/SMARTC-37299
        val imageToLoad = args.service.headerImage.takeUnless { it.isNullOrBlank() } ?: args.service.image
        binding.image.loadCenterCropped(imageToLoad)

        binding.reportDefectBtn.text = args.service.serviceAction?.first()?.visibleText
        binding.fullDescription.loadData(args.service.description, "text/html", "UTF-8")

        if (!args.service.helpLinkTitle.isNullOrBlank()) {
            binding.defectInfoButton.setVisible(true)
            binding.defectReporterInfoBtn.text = args.service.helpLinkTitle
            binding.defectInfoButton.setOnClickListener {
                findNavController().navigate(
                    DefectServiceDetailDirections.actionDefectServiceDetailToServiceHelp(args.service)
                )
            }
        }

        binding.reportDefectBtn.setOnClickListener {
            binding.reportDefectBtn.startLoading()
            viewModel.onOpenDefectReporterClicked()
        }
    }

    override fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.toolbarDefectReporter.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )
            insets
        }
        binding.nsvDefectReporter.dispatchInsetsToChildViews(
            binding.llcDefectReporterWebView,
            binding.reportDefectBtn
        ) { displayCutoutInsets ->
            binding.defectInfoButton.updatePadding(
                left = displayCutoutInsets.left + 21.dpToPixel(context),
                right = displayCutoutInsets.right + 21.dpToPixel(context)
            )
        }
    }

    fun subscribeUi() {
        viewModel.defectCategoryAvailable.observe(viewLifecycleOwner) {
            binding.reportDefectBtn.stopLoading()
            findNavController().navigate(DefectServiceDetailDirections.toDefectCategorySelection(args.service))
        }
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            context?.let {
                DialogUtil.showRetryDialog(it, viewModel::onRetryRequired) {
                    binding.reportDefectBtn.stopLoading()
                    viewModel.onRetryCanceled()
                }
            }
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.reportDefectBtn.stopLoading()
            context?.let { DialogUtil.showTechnicalError(it) }
        }
        viewModel.serviceError.observe(viewLifecycleOwner) {
            binding.reportDefectBtn.stopLoading()
            context?.let {
                DialogUtil.showInfoDialog(
                    context = it,
                    title = R.string.service_error_title,
                    message = R.string.service_error_description
                )
            }
        }
        viewModel.serviceUnavailable.observe(viewLifecycleOwner) {
            binding.reportDefectBtn.stopLoading()
            context?.let {
                DialogUtil.showInfoDialog(
                    context = it,
                    title = R.string.service_unavailable_title,
                    message = R.string.service_unavailable_description
                )
            }
        }
    }
}
