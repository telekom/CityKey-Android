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

package com.telekom.citykey.view.services.defect_reporter.category_selection.defectsubcategory

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectSubcategorySelectionFragmentBinding
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.defect_reporter.location_selection.DefectLocationSelection

class DefectSubcategorySelection : MainFragment(R.layout.defect_subcategory_selection_fragment) {
    private val binding by viewBinding(DefectSubcategorySelectionFragmentBinding::bind)
    private val args: DefectSubcategorySelectionArgs by navArgs()
    private var listAdapter: DefectSubcategorySelectionAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.defectSubcategoryToolbar)
        binding.defectSubcategoryToolbar.title = args.defectCategory.serviceName
        setUpAdapter()
    }

    private fun setUpAdapter() {
        listAdapter =
            DefectSubcategorySelectionAdapter(
                categoryResultListener = { defectSubcategory ->
                    DefectLocationSelection(
                        locationResultListener = {
                            findNavController().navigate(
                                DefectSubcategorySelectionDirections.toDefectReportForm2(
                                    args.service,
                                    it,
                                    args.defectCategory.serviceName,
                                    args.defectCategory.serviceCode,
                                    defectSubcategory.serviceName,
                                    defectSubcategory.serviceCode,
                                    defectSubcategory.description,
                                    defectSubcategory.hasAdditionalInfo ?: false
                                )
                            )
                        }
                    ).showDialog(
                        childFragmentManager
                    )
                }
            )
        binding.subategoryList.adapter = listAdapter
        listAdapter?.submitList(args.defectCategory.subCategories)
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
    }
}
