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

package com.telekom.citykey.view.services.fahrradparken.category_selection

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectCategorySelectionFragmentBinding
import com.telekom.citykey.models.defect_reporter.DefectCategory
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.defect_reporter.category_selection.DefectCategorySelectionAdapter
import com.telekom.citykey.view.services.fahrradparken.FahrradparkenService
import com.telekom.citykey.view.services.fahrradparken.location_selection.FahrradparkenLocationSelection
import org.koin.androidx.viewmodel.ext.android.viewModel

class FahrradparkenCategorySelection : MainFragment(R.layout.defect_category_selection_fragment) {

    private val viewModel: FahrradparkenCategorySelectionViewModel by viewModel()
    private val binding by viewBinding(DefectCategorySelectionFragmentBinding::bind)

    private val args: FahrradparkenCategorySelectionArgs by navArgs()
    private var listAdapter: DefectCategorySelectionAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initSubscribers()
    }

    private fun initViews() {
        with(binding) {
            setupToolbar(defectCategoryToolbar)
            listAdapter = DefectCategorySelectionAdapter(categoryResultListener = ::onCategorySelection)
            categoryList.adapter = listAdapter
        }
    }

    private fun initSubscribers() {
        viewModel.fahrradparkenCategories.observe(viewLifecycleOwner) {
            listAdapter?.submitList(it)
        }
    }

    private fun onCategorySelection(defectCategory: DefectCategory) {
        if (defectCategory.subCategories.isNullOrEmpty()) {

            if (args.isNewReport) {
                FahrradparkenLocationSelection(
                    defectCategory.serviceName ?: "",
                    defectCategory.serviceCode,
                    args.service.serviceParams?.get(FahrradparkenService.SERVICE_PARAM_MORE_INFO_BASE_URL),
                    locationResultListener = { navigateToFahrradparkenReportCreation(it, defectCategory) }
                ).showDialog(childFragmentManager)
            } else {
                findNavController().navigate(
                    FahrradparkenCategorySelectionDirections
                        .toExistingReportsFragment(
                            args.service,
                            args.isNewReport,
                            defectCategory,
                            null
                        )
                )
            }
        } else {
            findNavController().navigate(
                FahrradparkenCategorySelectionDirections
                    .actionFahrradparkenCategorySelectionToFahrradparkenSubcategorySelection(
                        args.service,
                        args.isNewReport,
                        defectCategory
                    )
            )
        }
    }

    private fun navigateToFahrradparkenReportCreation(latLng: LatLng, defectCategory: DefectCategory) {
        findNavController().navigate(
            FahrradparkenCategorySelectionDirections.toCreateReportForm(
                args.service,
                args.isNewReport,
                latLng,
                defectCategory,
                null
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
    }

}
