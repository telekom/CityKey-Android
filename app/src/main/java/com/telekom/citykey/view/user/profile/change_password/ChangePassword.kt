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

package com.telekom.citykey.view.user.profile.change_password

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.custom.views.inputfields.OscaInputLayout
import com.telekom.citykey.databinding.ProfileChangePasswordFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePassword : Fragment(R.layout.profile_change_password_fragment) {

    private val viewModel: ChangePasswordViewModel by viewModel()
    private val binding by viewBinding(ProfileChangePasswordFragmentBinding::bind)

    private val changePwdFields: Map<String, OscaInputLayout> by lazy {
        mapOf(
            ChangePassField.CURRENT_PASSWORD to binding.currentPassword,
            ChangePassField.PASSWORD to binding.newPassword,
            ChangePassField.SEC_PASSWORD to binding.repeatNewPassword,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as ProfileActivity).apply {
            setPageTitle(R.string.p_005_profile_password_change_title)
            adaptToolbarForBack()
        }

        binding.contentParent.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.accessRightsBtn.disable()
        binding.repeatNewPassword.deactivate()
        binding.forgotPasswordButton.setAccessibilityRole(AccessibilityRole.Button)
        initPasswordStrengthView()
        initListeners()

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = {
                    binding.accessRightsBtn.startLoading()
                    viewModel.onRetryRequired()
                },
                onCancel = viewModel::onRetryCanceled
            )
        }
        viewModel.stopLoading.observe(viewLifecycleOwner) { binding.accessRightsBtn.stopLoading() }

        viewModel.passwordStrength.observe(viewLifecycleOwner) {
            binding.passwordStrength.updateValidation(it)
            if (it.percentage == 100) {
                binding.repeatNewPassword.activate()
            } else {
                binding.repeatNewPassword.deactivate()
            }
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) {
            changePwdFields[it.first]?.validation = it.second
            updateButtonStatus()
        }

        viewModel.saveSuccessful.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_changePassword_to_passwordChanged)
        }
        viewModel.logUserOut.observe(viewLifecycleOwner) {
            (activity as ProfileActivity).logOut()
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
        }
    }

    private fun updateButtonStatus() {
        if (areFieldsOk() && areFieldsFilled()) {
            binding.accessRightsBtn.enable()
        } else {
            binding.accessRightsBtn.disable()
        }
    }

    private fun areFieldsFilled() = binding.currentPassword.text.isNotBlank() &&
        binding.newPassword.text.isNotBlank() &&
        binding.repeatNewPassword.text.isNotBlank()

    private fun areFieldsOk() = !binding.currentPassword.hasErrors &&
        !binding.newPassword.hasErrors &&
        !binding.repeatNewPassword.hasErrors

    private fun initPasswordStrengthView() {
        val hints = resources.getStringArray(R.array.hints)
        val hintsFormat = getString(R.string.r_001_registration_hint_password_strength)

        var hintsString = ""
        hints.forEach { hintsString += "$it, " }
        hintsString = hintsString.substring(0, hintsString.length - 2)

        val formattedHintsString = String.format(hintsFormat, hintsString)

        binding.passwordStrength.initValidation(formattedHintsString)
        viewModel.onPasswordHintsResourcesReceived(hints, formattedHintsString)
    }

    private fun initListeners() {
        binding.newPassword.onTextChanged {
            binding.repeatNewPassword.clear()
            viewModel.onPasswordTextChanged(it)
        }

        binding.currentPassword.onTextChanged {
            updateButtonStatus()
        }

        binding.repeatNewPassword.onTextChanged {
            if (it.isNotBlank()) {
                viewModel.onSecondPasswordTextChanged(binding.newPassword.text to binding.repeatNewPassword.text)
            } else {
                binding.repeatNewPassword.validation = FieldValidation(FieldValidation.IDLE, null)
            }
        }

        binding.accessRightsBtn.setOnClickListener {
            binding.accessRightsBtn.startLoading()
            viewModel.onSaveButtonClicked(binding.currentPassword.text, binding.newPassword.text)
        }

        binding.forgotPasswordButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_changePassword_to_forgotPassword)
        }
    }
}
