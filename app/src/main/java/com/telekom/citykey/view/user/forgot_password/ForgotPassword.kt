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

package com.telekom.citykey.view.user.forgot_password

import android.animation.LayoutTransition
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.RegistrationConfirmationGraphArgs
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.custom.views.inputfields.OscaInputLayout
import com.telekom.citykey.databinding.ForgotPasswordFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.pin_verification.PINVerificationActions
import com.telekom.citykey.view.user.pin_verification.success.VerificationSuccess
import com.telekom.citykey.view.user.profile.ProfileActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPassword : Fragment(R.layout.forgot_password_fragment) {

    private val viewModel: ForgotPasswordViewModel by viewModel()
    private val binding by viewBinding(ForgotPasswordFragmentBinding::bind)

    private val fpFields: Map<String, OscaInputLayout> by lazy {
        mapOf(
            ForgotPasswordFields.EMAIL to binding.emailInput,
            ForgotPasswordFields.PASSWORD to binding.passwordInput,
            ForgotPasswordFields.SEC_PASSWORD to binding.repeatPasswordInput
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (requireActivity()) {
            is LoginActivity -> (activity as LoginActivity).run {
                adaptToolbarForBack()
                binding.header.setVisible(false)
                setTopIcon(R.drawable.ic_icon_reset_password)
                setPageTitle(R.string.f_001_forgot_password_title)
                binding.details.setText(R.string.f_001_forgot_password_info_click_link)
            }
            is ProfileActivity -> (activity as ProfileActivity).run {
                adaptToolbarForBack()
                binding.header.setVisible(true)
                setPageTitle(R.string.f_001_forgot_password_title)
                binding.details.setText(R.string.f_001_forgot_password_info_click_link)
            }
        }

        binding.contentBox.layoutTransition =
            LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }

        initPasswordStrengthView()
        initListeners()
        initViews()
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = { binding.resetButton.startLoading(); viewModel.onRetryRequired() },
                onCancel = viewModel::onRetryCanceled
            )
            binding.resetButton.stopLoading()
        }

        viewModel.passwordStrength.observe(viewLifecycleOwner) {
            binding.passwordStrength.updateValidation(it)
            if (it.percentage == 100) {
                binding.repeatPasswordInput.activate()
            } else {
                binding.repeatPasswordInput.deactivate()
            }
        }

        viewModel.userEmail.observe(viewLifecycleOwner) {
            binding.emailInput.text = it
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) {
            fpFields[it.first]?.validation = it.second
            binding.resetButton.stopLoading()
            toggleButtonState()
        }

        viewModel.openPinVerification.observe(viewLifecycleOwner) {
            findNavController().navigate(
                R.id.action_forgotPassword_to_registration_confirmation_graph,
                RegistrationConfirmationGraphArgs.Builder(
                    "", "",
                    binding.emailInput.text,
                    VerificationSuccess.PASSWORD_CHANGED,
                    PINVerificationActions.PASSWORD_RECOVERY_RESEND,
                    PINVerificationActions.PASSWORD_RECOVERY_VALIDATION,
                    R.string.f_001_forgot_password_title,
                    R.string.f_002_forgot_pwd_confirmation_info_enter_pin,
                    R.string.r_004_registration_confirmation_info_sent_mail,
                    null
                ).build().toBundle()
            )
        }

        viewModel.openVerifyEmail.observe(viewLifecycleOwner) {
            findNavController().navigate(
                R.id.action_forgotPassword_to_registration_confirmation_graph,
                RegistrationConfirmationGraphArgs.Builder(
                    "", "",
                    binding.emailInput.text,
                    VerificationSuccess.REGISTRATION_CONFIRMED,
                    PINVerificationActions.REGISTRATION_RESEND,
                    PINVerificationActions.REGISTRATION_VALIDATION,
                    R.string.r_004_registration_confirmation_title,
                    R.string.r_004_registration_confirmation_info_enter_pin,
                    R.string.l_004_login_confirm_email_info_check_inbox,
                    it
                ).build().toBundle()
            )
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
            binding.resetButton.stopLoading()
        }
    }

    private fun initListeners() {
        binding.resetButton.setOnClickListener {
            binding.resetButton.startLoading()
            viewModel.onResetClicked(binding.emailInput.text, binding.passwordInput.text)
        }

        val emptyTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleButtonState()
            }
        }

        binding.emailInput.editText.addTextChangedListener(emptyTextWatcher)
        binding.passwordInput.editText.addTextChangedListener(emptyTextWatcher)
        binding.repeatPasswordInput.editText.addTextChangedListener(emptyTextWatcher)

        binding.passwordInput.onTextChanged {
            binding.repeatPasswordInput.clear()
            viewModel.onPasswordTextChanged(it)
        }

        binding.passwordInput.onFocusChanged { focused ->
            if (!focused && binding.passwordInput.text.isEmpty())
                binding.passwordInput.validation =
                    FieldValidation(FieldValidation.ERROR, null, R.string.r_001_registration_error_empty_field)
        }

        binding.repeatPasswordInput.onFocusChanged {
            if (!it && binding.repeatPasswordInput.text.isNotBlank() && binding.repeatPasswordInput.text != binding.passwordInput.text) {
                binding.repeatPasswordInput.validation = FieldValidation(
                    FieldValidation.ERROR,
                    getString(R.string.r_001_registration_error_password_no_match)
                )
            }
        }

        binding.emailInput.onFocusChanged { focused ->
            if (!focused) viewModel.onEmailReady(binding.emailInput.text)
        }

        binding.repeatPasswordInput.onTextChanged {
            if (it.isNotBlank()) {
                viewModel.onSecondPasswordTextChanged(binding.passwordInput.text to binding.repeatPasswordInput.text)
            } else {
                binding.repeatPasswordInput.validation = FieldValidation(FieldValidation.IDLE, null)
            }
        }
    }

    private fun initViews() {
        arguments?.getString("email")?.let { binding.emailInput.text = it }
    }

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

    private fun toggleButtonState() {
        if (areFieldsErrorFree && areFieldsNotEmpty) {
            binding.resetButton.enable()
        } else {
            binding.resetButton.disable()
        }
    }

    private val areFieldsErrorFree
        get() =
            !binding.emailInput.hasErrors && !binding.passwordInput.hasErrors && !binding.repeatPasswordInput.hasErrors

    private val areFieldsNotEmpty
        get() =
            binding.emailInput.text.isNotBlank() && binding.passwordInput.text.isNotBlank() && binding.repeatPasswordInput.text.isNotBlank()
}
