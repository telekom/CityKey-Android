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

package com.telekom.citykey.view.services.poi.categories

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.widget.TextViewCompat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiCategoriesFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.poi.PoiCategory
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class PoiCategorySelection(
    private val category: PoiCategory?,
    private var onDismiss: (Boolean) -> Unit
) : FullScreenBottomSheetDialogFragment(R.layout.poi_categories_fragment) {

    private val binding by viewBinding(PoiCategoriesFragmentBinding::bind)

    private val viewModel: PoiCategorySelectionViewModel by viewModel()
    private var listAdapter: PoiCategorySelectionAdapter? = null

    private var isCategorySelected: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarPoiCategory.navigationContentDescription =
            getString(R.string.accessibility_btn_close)
        binding.toolbarPoiCategory.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarPoiCategory.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarPoiCategory.setNavigationOnClickListener {
            dismiss()
        }
        setAccessibilityRoleForToolbarTitle(binding.toolbarPoiCategory)

        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )
        binding.retryButton.setOnClickListener {
            binding.errorLayout.setVisible(false)
            binding.loading.setVisible(true)
            viewModel.onRetry()
        }

        listAdapter = PoiCategorySelectionAdapter(viewModel::onCategorySelected, category)
        binding.categoryServicesList.adapter = listAdapter
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.categoryListItems.observe(viewLifecycleOwner) { categories ->
            binding.loading.setVisible(false)
            binding.errorLayout.setVisible(categories.isEmpty())
            binding.categoryServicesList.setVisible(categories.isNotEmpty())
            if (categories.isNotEmpty()) listAdapter?.submitList(categories)
        }
        viewModel.poiDataAvailable.observe(viewLifecycleOwner) {
            isCategorySelected = true
            dismiss()
        }
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                viewModel.onRetryCanceled()
                listAdapter?.stopLoading()
            }
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            listAdapter?.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismiss(isCategorySelected)
        super.onDismiss(dialog)
        listAdapter = null
    }
}
