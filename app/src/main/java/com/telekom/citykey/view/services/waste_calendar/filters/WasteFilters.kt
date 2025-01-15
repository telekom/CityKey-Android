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

@file:Suppress("OVERRIDE_DEPRECATION")

package com.telekom.citykey.view.services.waste_calendar.filters

import android.appwidget.AppWidgetManager
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.size
import com.google.android.material.chip.Chip
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WastecalendarCategoryFilterFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class WasteFilters(
    val resultListener: (Boolean) -> Unit
) : FullScreenBottomSheetDialogFragment(R.layout.wastecalendar_category_filter_fragment) {

    private val viewModel: WasteFiltersViewModel by viewModel()
    private val binding by viewBinding(WastecalendarCategoryFilterFragmentBinding::bind)

    private val clearAllMenuItem: MenuItem by lazy {
        binding.toolbarCategoryFilter.menu.findItem(R.id.actionClearAll)
    }
    private var isSuccess = false

    companion object {
        const val FRAGMENT_TAG_FILTERS = "filters"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
        adjustLayoutByOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustLayoutByOrientation()
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
            binding.applyFilterBtn.startLoading()
            val newFilters = mutableListOf<String>()
            binding.categoriesContainer.forEach { chip ->
                if ((chip as Chip).isChecked) newFilters.add(chip.tag.toString())
            }
            viewModel.onCategoryFiltersAccepted(newFilters)
            AppWidgetManager.getInstance(requireContext()).updateWasteCalendarWidget(requireContext())
        }
        val spannableString = SpannableString(clearAllMenuItem.title.toString())
        spannableString.setSpan(ForegroundColorSpan(CityInteractor.cityColorInt), 0, spannableString.length, 0)
        clearAllMenuItem.title = spannableString
        binding.applyFilterBtn.button.setBackgroundColor(CityInteractor.cityColorInt)
        binding.selectToggleBtn.setupOutlineStyle(CityInteractor.cityColorInt)
        binding.selectToggleBtn.setOnClickListener { selectAllCategories() }
    }

    private fun subscribeUi() {
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                dismiss()
            }
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.applyFilterBtn.stopLoading()
            binding.progressCategories.setVisible(false)
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.categories.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                makeButtonVisibilityGone()
                binding.categoriesErrorHint.setVisible(true)
                binding.applyFilterBtn.stopLoadingAfterError()
                return@observe
            } else {
                makeButtonVisibilityVisible()
                binding.categoriesErrorHint.setVisible(false)
            }

            it.forEach { category ->
                binding.categoriesContainer.addView(generateChip(category.name, category.id))
            }
            viewModel.selectedPickupStatus.observe(viewLifecycleOwner) {
                binding.applyFilterBtn.stopLoading()
                isSuccess = true
                dismiss()
            }
        }

        viewModel.appliedFilters.observe(viewLifecycleOwner) { filters ->
            updateProgressBtnInfo()
            binding.progressCategories.setVisible(false)
            binding.applyFilterBtn.stopLoading()
            binding.categoriesContainer.post {
                safeRun {
                    filters.forEach {
                        binding.categoriesContainer.findViewWithTag<Chip>(it.toInt())?.isChecked = true
                    }
                }
                if (areAllCategoriesUnchecked()) clearAllMenuItem.isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.actionClearAll -> {
            clearFilter()
            clearAllMenuItem.isVisible = false
            true
        }

        android.R.id.home -> {
            requireActivity().onBackPressed()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun clearFilter() {
        binding.categoriesContainer.clearCheck()
    }

    private fun selectAllCategories() {
        binding.categoriesContainer.forEach { category ->
            (category as Chip).isChecked = true
        }
    }

    private fun areAllCategoriesUnchecked(): Boolean {
        binding.categoriesContainer.forEach { category ->
            if ((category as Chip).isChecked) {
                return false
            }
        }
        return true
    }

    private fun updateProgressBtnInfo() {
        val selectionCount = binding.categoriesContainer.children.toList().filter { (it as Chip).isChecked }.size
        binding.applyFilterBtn.text = getString(R.string.wc_004_filter_category_show_result, selectionCount)
        if (binding.categoriesContainer.size == selectionCount) {
            binding.selectToggleBtn.setOnClickListener { clearFilter() }
            binding.selectToggleBtn.setText(R.string.wc_004_filter_category_deselect_all)
        } else {
            binding.selectToggleBtn.setOnClickListener { selectAllCategories() }
            binding.selectToggleBtn.setText(R.string.wc_004_filter_category_select_all)
        }
    }

    private fun generateChip(category: String, categoryid: Int): Chip {
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
        chip.text = category
        chip.tag = categoryid
        chip.setAccessibilityBasedOnViewStateSelection(chip.isSelected)

        chip.setOnCheckedChangeListener { _, isChecked ->
            chip.setAccessibilityBasedOnViewStateSelection(isChecked)
            updateProgressBtnInfo()
            if (isChecked) {
                clearAllMenuItem.isVisible = true
            } else {
                if (areAllCategoriesUnchecked()) clearAllMenuItem.isVisible = false
            }
        }
        return chip
    }

    override fun onDismiss(dialog: DialogInterface) {
        resultListener(isSuccess)
        super.onDismiss(dialog)
    }

    private fun makeButtonVisibilityGone() {
        binding.selectToggleBtn.visibility = View.GONE
        binding.applyFilterBtn.visibility = View.GONE
        binding.wasteCalendarPickupsDivider.visibility = View.GONE
    }

    private fun makeButtonVisibilityVisible() {
        binding.selectToggleBtn.visibility = View.VISIBLE
        binding.applyFilterBtn.visibility = View.VISIBLE
        if (!isInLandscapeOrientation) {
            binding.wasteCalendarPickupsDivider.visibility = View.VISIBLE
        }
    }

    /**
     * Since we cannot load a new layout or perform a context change by ourselves, we change the buttons' constraints
     * in the existing ConstraintLayout to match the landscape mode
     */
    private fun createLandscapeLayout() {
        val constraintLayout: ConstraintLayout = binding.root
        val set = ConstraintSet()
        set.clone(constraintLayout)

        // Select button
        set.connect(binding.selectToggleBtn.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 24)
        set.connect(binding.selectToggleBtn.id, ConstraintSet.END, binding.applyFilterBtn.id, ConstraintSet.START)
        set.connect(binding.selectToggleBtn.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 36)

        // Apply button
        set.connect(binding.applyFilterBtn.id, ConstraintSet.START, binding.selectToggleBtn.id, ConstraintSet.END)
        set.connect(binding.applyFilterBtn.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 24)
        set.connect(binding.applyFilterBtn.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 36)

        set.applyTo(constraintLayout)
        binding.selectToggleBtn.setButtonWidthMatchParent()
        binding.applyFilterBtn.setButtonWidthMatchParent()
    }

    /**
     * Since we cannot load a new layout or perform a context change by ourselves, we change the buttons' constraints
     * in the existing ConstraintLayout to match the portrait mode
     */
    private fun createPortraitLayout() {
        val constraintLayout: ConstraintLayout = binding.root
        val set = ConstraintSet()
        set.clone(constraintLayout)

        // Select button
        set.connect(binding.selectToggleBtn.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        set.connect(binding.selectToggleBtn.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        set.connect(binding.selectToggleBtn.id, ConstraintSet.BOTTOM, binding.applyFilterBtn.id, ConstraintSet.TOP)

        // Apply button
        set.connect(binding.applyFilterBtn.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        set.connect(binding.applyFilterBtn.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        set.connect(binding.applyFilterBtn.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 36)

        set.applyTo(constraintLayout)
        binding.selectToggleBtn.setButtonWidthWrapContent()
        binding.applyFilterBtn.setButtonWidthWrapContent()
    }

    /**
     * Change required layout when an Orientation change is detected
     */
    private fun adjustLayoutByOrientation() {
        if (isInLandscapeOrientation) {
            createLandscapeLayout()
        } else {
            createPortraitLayout()
        }

        binding.wasteCalendarPickupsDivider.isVisible = !isInLandscapeOrientation
    }
}
