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

package com.telekom.citykey.view.services.waste_calendar.address_change

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.databinding.WasteCalendarAddressFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.EmptyTextWatcher
import com.telekom.citykey.utils.KoverIgnore
import com.telekom.citykey.utils.extensions.applySafeAllInsetsWithSides
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.fadeIn
import com.telekom.citykey.utils.extensions.fadeOut
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.isInLandscapeOrientation
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@KoverIgnore
class WasteCalendarAddress(
    val resultListener: (Boolean) -> Unit
) : FullScreenBottomSheetDialogFragment(R.layout.waste_calendar_address_fragment, true) {

    private val viewModel: WasteCalendarAddressViewModel by viewModel()
    private val binding by viewBinding(WasteCalendarAddressFragmentBinding::bind)
    private var streetAdapter: StreetAddressAdapter? = null
    private var houseAdapter: ArrayAdapter<String>? = null
    private var isSuccess = false
    private var isAddressAvailable = true
    private var isHouseFieldAvailable = false

    @KoverIgnore
    companion object {
        const val FRAGMENT_TAG_ADDRESS = "address"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        initViews()
        handleWindowInsets()
        subscribeUi()
        adjustLayoutByOrientation()
    }

    private fun handleWindowInsets() {
        binding.appBarLayout.applySafeAllInsetsWithSides(left = true, right = true)
        binding.streetName.applySafeAllInsetsWithSides(left = true, right = true)
        binding.houseNumber.applySafeAllInsetsWithSides(left = true, right = true)
        binding.cityName.applySafeAllInsetsWithSides(left = true, right = true)
        binding.progressBtnAddress.applySafeAllInsetsWithSides(left = true, right = true, bottom = true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustLayoutByOrientation()
    }

    private fun subscribeUi() {
        viewModel.ftuAddress.observe(viewLifecycleOwner) {
            streetAdapter?.updateSuggestions(it)
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) {
            if (it.first == "streetName" && binding.streetName.text.isNotEmpty()) {
                binding.streetName.validation = it.second
            } else {
                binding.houseNumber.validation = it.second
            }
            updateButtonStatus()
        }

        viewModel.availableHouseNumbers.observe(viewLifecycleOwner) { availableHouseNumbers ->
            val isOk = availableHouseNumbers.isNotEmpty()
            if (isOk) {
                houseAdapter?.clear()
                houseAdapter?.addAll(availableHouseNumbers)
                binding.houseNumber.activate()
                binding.houseNumber.fadeIn()
                    .subscribe {
                        lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                binding.houseNumber.requestFocusAtEnd()
                            }
                        }
                    }
            }
            isHouseFieldAvailable = isOk
            updateButtonStatus()
        }

        viewModel.cityData.observe(viewLifecycleOwner) {
            binding.cityName.text = it
        }

        viewModel.wasteCalendarAvailable.observe(viewLifecycleOwner) {
            isSuccess = true
            dismiss()
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.progressBtnAddress.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.error.observe(viewLifecycleOwner) {
            binding.progressBtnAddress.stopLoading()
            binding.streetName.validation = it
            binding.houseNumber.validation = it
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                binding.progressBtnAddress.stopLoading()
                viewModel.onRetryCanceled()
            }
        }

        viewModel.currentAddress.observe(viewLifecycleOwner) {
            if (it.streetName.isEmpty() && it.houseNumber.isEmpty()) {
                isAddressAvailable = false
            }
            binding.streetName.text = it.streetName
            binding.houseNumber.text = it.houseNumber
            if (isAddressAvailable) {
                binding.streetName.requestFocusAtEnd()
            }
        }

        viewModel.userLoggedOut.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.services, false)
        }
    }

    private fun initViews() {
        binding.houseNumber.deactivate()
        binding.cityName.deactivate()
        binding.progressBtnAddress.disable()
        binding.progressBtnAddress.setupNormalStyle(CityInteractor.cityColorInt)
        binding.layoutContent.setBackgroundColor(CityInteractor.cityColorInt)
        streetAdapter = StreetAddressAdapter(requireContext(), R.layout.street_address_ftu_item)
        binding.streetName.editText.setAdapter(streetAdapter)
        houseAdapter = ArrayAdapter<String>(requireContext(), R.layout.street_address_ftu_item)
        binding.houseNumber.editText.setAdapter(houseAdapter)

        binding.streetName.onTextChanged {
            if (binding.houseNumber.isVisible) {
                binding.houseNumber.deactivate()
                binding.houseNumber.clear()
                binding.houseNumber.fadeOut()
                    .subscribe()
            }

            binding.houseNumber.post {
                viewModel.onStreetTextChanged(it, binding.houseNumber.text)
            }
        }

        binding.houseNumber.onTextChanged {
            if (binding.streetName.validation.state != FieldValidation.OK) return@onTextChanged
            viewModel.onHouseNumberChange(it)
        }

        binding.progressBtnAddress.setOnClickListener {
            if (isAddressAvailable) {
                MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
                    .setTitle(R.string.wc_004_change_address_reset_reminder_title)
                    .setMessage(R.string.wc_004_change_address_reset_reminder_info)
                    .setPositiveButton(R.string.wc_004_change_address_reset_reminder_ok) { _, _ ->
                        binding.progressBtnAddress.startLoading()
                        viewModel.onOpenWasteCalendarClicked(binding.streetName.text, binding.houseNumber.text)
                    }
                    .setNegativeButton(R.string.wc_004_change_address_reset_reminder_cancel, null)
                    .show()
            } else {
                binding.progressBtnAddress.startLoading()
                viewModel.onOpenWasteCalendarClicked(binding.streetName.text, binding.houseNumber.text)
            }
        }
        setBehaviorListeners()
    }

    private fun setBehaviorListeners() {
        val textWatcher = object : EmptyTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                updateButtonStatus()
            }
        }
        binding.streetName.editText.addTextChangedListener(textWatcher)
        binding.houseNumber.editText.addTextChangedListener(textWatcher)
    }

    private fun updateButtonStatus() {
        if (areFieldsErrorFree() && areFieldsFilled()) {
            binding.progressBtnAddress.enable()
        } else {
            binding.progressBtnAddress.disable()
        }
    }

    private fun areFieldsFilled() = if (isHouseFieldAvailable)
        binding.streetName.text.isNotBlank() && binding.houseNumber.text.isNotBlank()
    else
        binding.streetName.text.isNotBlank()

    private fun areFieldsErrorFree() = !binding.streetName.hasErrors &&
            !binding.houseNumber.hasErrors

    override fun onDismiss(dialog: DialogInterface) {
        resultListener(isSuccess)
        houseAdapter = null
        streetAdapter = null
        super.onDismiss(dialog)
    }

    /**
     * Since we cannot load a new layout or perform a context change by ourselves, we change the fields' constraints
     * in the existing ConstraintLayout to match the portrait mode
     */
    private fun createPortraitLayout() {
        val constraintLayout: ConstraintLayout = binding.root
        val set = ConstraintSet()
        set.clone(constraintLayout)

        // Street
        set.connect(binding.streetName.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(binding.streetName.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(binding.streetName.id, ConstraintSet.TOP, binding.layoutContent.id, ConstraintSet.BOTTOM, 40)

        // House
        set.connect(binding.houseNumber.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(binding.houseNumber.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(binding.houseNumber.id, ConstraintSet.TOP, binding.streetName.id, ConstraintSet.BOTTOM, 28)

        // City
        set.connect(binding.cityName.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(binding.cityName.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(binding.cityName.id, ConstraintSet.TOP, binding.houseNumber.id, ConstraintSet.BOTTOM, 28)

        set.applyTo(constraintLayout)
        binding.progressBtnAddress.setButtonWidthWrapContent()
    }

    /**
     * Since we cannot load a new layout or perform a context change by ourselves, we change the fields' constraints
     * in the existing ConstraintLayout to match the landscape mode
     */
    private fun createLandscapeLayout() {
        val constraintLayout: ConstraintLayout = binding.root
        val set = ConstraintSet()
        set.clone(constraintLayout)

        // Street
        set.connect(binding.streetName.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(binding.streetName.id, ConstraintSet.END, binding.houseNumber.id, ConstraintSet.START)
        set.connect(binding.streetName.id, ConstraintSet.TOP, binding.layoutContent.id, ConstraintSet.BOTTOM, 40)

        // House
        set.connect(binding.houseNumber.id, ConstraintSet.START, binding.streetName.id, ConstraintSet.END)
        set.connect(binding.houseNumber.id, ConstraintSet.END, binding.cityName.id, ConstraintSet.START)
        set.connect(binding.houseNumber.id, ConstraintSet.TOP, binding.layoutContent.id, ConstraintSet.BOTTOM, 40)

        // City
        set.connect(binding.cityName.id, ConstraintSet.START, binding.houseNumber.id, ConstraintSet.END)
        set.connect(binding.cityName.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(binding.cityName.id, ConstraintSet.TOP, binding.layoutContent.id, ConstraintSet.BOTTOM, 40)

        set.applyTo(constraintLayout)
        binding.progressBtnAddress.setButtonWidthMatchParent()
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

        binding.layoutContent.isVisible = !isInLandscapeOrientation
        binding.wasteCalendarDetailsDivider.isVisible = !isInLandscapeOrientation
    }
}
