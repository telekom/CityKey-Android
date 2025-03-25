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

package com.telekom.citykey.view.services.defect_reporter.category_selection

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectCategorySelectionFragmentBinding
import com.telekom.citykey.utils.extensions.applySafeAllInsetsWithSides
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.defect_reporter.location_selection.DefectLocationSelection
import org.koin.androidx.viewmodel.ext.android.viewModel

class DefectCategorySelection : MainFragment(R.layout.defect_category_selection_fragment) {
    private val viewModel: DefectCategorySelectionViewModel by viewModel()
    private val binding by viewBinding(DefectCategorySelectionFragmentBinding::bind)
    private var listAdapter: DefectCategorySelectionAdapter? = null
    private val args: DefectCategorySelectionArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.defectCategoryToolbar)
        setUpAdapter()
        subscribeUi()
    }

    override fun handleWindowInsets() {
        super.handleWindowInsets()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.defectCategoryToolbar.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )
            insets
        }
        binding.categoryList.applySafeAllInsetsWithSides(left = true, right = true, bottom = true)
    }

    fun subscribeUi() {
        viewModel.defectReporterCategories.observe(viewLifecycleOwner) {
            listAdapter?.submitList(it)
        }
    }

    private fun setUpAdapter() {
        listAdapter = DefectCategorySelectionAdapter(
            categoryResultListener = { defectCategory ->
                if (defectCategory.subCategories.isNullOrEmpty()) {
                    DefectLocationSelection(
                        locationResultListener = {
                            findNavController().navigate(
                                DefectCategorySelectionDirections.toDefectReportForm2(
                                    args.service,
                                    it,
                                    defectCategory.serviceName,
                                    defectCategory.serviceCode,
                                    null,
                                    null,
                                    null,
                                    false
                                )
                            )
                        }
                    ).showDialog(
                        childFragmentManager
                    )
                } else {
                    findNavController().navigate(
                        DefectCategorySelectionDirections.actionDefectCategorySelectionToDefectSubcategorySelection(
                            args.service,
                            defectCategory
                        )
                    )
                }
            }
        )
        binding.categoryList.adapter = listAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
    }
}
