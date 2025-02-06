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

package com.telekom.citykey.view.home.events_list.category_filter

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.core.view.setPadding
import com.google.android.material.chip.Chip
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EventsCategoryFilterBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.content.EventCategory
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityBasedOnViewStateSelection
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.android.ext.android.inject

class CategoryFilter : FullScreenBottomSheetDialogFragment(R.layout.events_category_filter) {
    private val viewModel: CategoryFilterViewModel by inject()
    private val binding by viewBinding(EventsCategoryFilterBinding::bind)

    private val clearAllMenuItem: MenuItem by lazy {
        binding.toolbarCategoryFilter.menu.findItem(R.id.actionClearAll)
    }
    private var isSuccess = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
    }

    fun initViews() {
        binding.toolbarCategoryFilter.inflateMenu(R.menu.categories_filter_menu)
        binding.toolbarCategoryFilter.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarCategoryFilter.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbarCategoryFilter.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarCategoryFilter.setNavigationOnClickListener { dismiss() }
        binding.toolbarCategoryFilter.setOnMenuItemClickListener {
            if (it.itemId == R.id.actionClearAll) {
                clearFilter()
                clearAllMenuItem.isVisible = false
            }
            true
        }
        setAccessibilityRoleForToolbarTitle(binding.toolbarCategoryFilter)
        binding.applyFilterBtn.setOnClickListener {
            viewModel.confirmFiltering()
            isSuccess
            dismiss()
        }

        val spannableString = SpannableString(clearAllMenuItem.title.toString())
        spannableString.setSpan(ForegroundColorSpan(CityInteractor.cityColorInt), 0, spannableString.length, 0)
        clearAllMenuItem.title = spannableString

        binding.applyFilterBtn.button.setBackgroundColor(CityInteractor.cityColorInt)
    }

    private fun clearFilter() {
        binding.categoriesContainer.forEach { category ->
            if ((category as Chip).isChecked) {
                category.isChecked = false
            }
        }
        viewModel.onFiltersCleared()
    }

    private fun areAllCategoriesUnchecked(): Boolean {
        binding.categoriesContainer.forEach { category ->
            if ((category as Chip).isChecked) {
                return false
            }
        }
        return true
    }

    private fun generateChip(category: EventCategory): Chip {
        val chip = Chip(requireContext())
        chip.chipBackgroundColor = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(getColor(R.color.background), CityInteractor.cityColorInt)
        )
        chip.chipStrokeColor = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(getColor(R.color.onSurfaceSecondary), CityInteractor.cityColorInt)
        )
        chip.setPadding(0)
        chip.chipStrokeWidth = 1f.dpToPixel(requireContext())
        chip.text = category.categoryName
        chip.tag = category.id
        chip.setAccessibilityBasedOnViewStateSelection(chip.isSelected)

        chip.setOnCheckedChangeListener { _, isChecked ->
            chip.setAccessibilityBasedOnViewStateSelection(isChecked)
            if (isChecked) {
                viewModel.onCategoryAdded(Integer.parseInt(chip.tag.toString()))
                clearAllMenuItem.isVisible = true
            } else {
                viewModel.onCategoryRemoved(Integer.parseInt(chip.tag.toString()))
                if (areAllCategoriesUnchecked())
                    clearAllMenuItem.isVisible = false
            }
        }
        return chip
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!isSuccess)
            viewModel.revokeFiltering()
        super.onDismiss(dialog)
    }

    private fun subscribeUi() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categoriesList ->
            binding.progressCategories.setVisible(false)
            if (categoriesList.isEmpty()) {
                binding.categoriesErrorHint.setVisible(true)
                binding.applyFilterBtn.stopLoadingAfterError()
                return@observe
            } else {
                binding.categoriesErrorHint.setVisible(false)
                binding.applyFilterBtn.stopLoading()
            }

            categoriesList.forEach { category ->
                if (category.id != null && category.categoryName.isNullOrBlank().not()) {
                    binding.categoriesContainer.addView(generateChip(category))
                }
            }
        }

        viewModel.eventsCount.observe(viewLifecycleOwner) {
            val buttonText = if (it == null) getString(R.string.e_003_show_events_button)
            else String.format("%s (%d)", getString(R.string.e_003_show_events_button), it)
            binding.applyFilterBtn.text = buttonText
        }

        viewModel.filters.observe(viewLifecycleOwner) { filterIds ->
            binding.categoriesContainer.forEach { chip ->
                filterIds?.forEach forEachIds@{ id ->
                    if (chip.tag == id) {
                        (chip as Chip).isChecked = true
                        return@forEachIds
                    }
                }
            }
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = {
                    binding.progressCategories.setVisible(true)
                    viewModel.onRetryRequired()
                },
                onCancel = {
                    viewModel.revokeFiltering()
                    isSuccess = false
                    dismiss()
                }
            )
        }
    }
}
