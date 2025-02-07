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

package com.telekom.citykey.view.user.registration

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.RegistrationConfirmationGraphArgs
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.custom.views.inputfields.OscaInputLayout
import com.telekom.citykey.databinding.RegistrationFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.EmptyTextWatcher
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityBasedOnViewStateSelection
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.dialogs.DataPrivacyNoticeDialog
import com.telekom.citykey.view.user.pin_verification.PINVerificationActions
import com.telekom.citykey.view.user.pin_verification.success.VerificationSuccess
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel

class Registration : Fragment(R.layout.registration_fragment) {
    private val viewModel: RegistrationViewModel by viewModel()
    private val binding by viewBinding(RegistrationFragmentBinding::bind)

    private val regFields: Map<String, OscaInputLayout> by lazy {
        mapOf(
            RegFields.EMAIL to binding.emailInput,
            RegFields.PASSWORD to binding.passwordInput,
            RegFields.BIRTHDAY to binding.birthdayInput,
            RegFields.SEC_PASSWORD to binding.repeatPasswordInput,
            RegFields.POSTAL_CODE to binding.postCodeInput
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contentLayout.layoutTransition =
            LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }

        (requireActivity() as RegistrationActivity).run {
            setToolbarTitle(R.string.r_001_registration_title)
            setTopIcon(R.drawable.ic_icon_register)
        }

        initPasswordStrengthView()
        initViews()
        setupCheckboxes()
        subscribeUi()
    }

    private fun setupCheckboxes() {
        val privacy = getString(R.string.r_001_registration_label_agreement_privacy)
        val privacyText = getString(R.string.r_001_registration_label_agreement_abstract, privacy)
        val indexOfPrivacy = privacyText.indexOf(privacy)

        val privacySpan = SpannableString(privacyText)

        privacySpan.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    DataPrivacyNoticeDialog().showDialog(childFragmentManager)
                }
            },
            indexOfPrivacy, indexOfPrivacy + privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.checkPrivacy.apply {
            text = privacySpan
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(getColor(R.color.oscaColor))
        }
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

    private fun initViews() {
        binding.repeatPasswordInput.deactivateChildren()
        binding.registerBtn.disable()

        initPasswordViews()
        initBirthdayView()

        binding.emailInput.onTextChanged {
            viewModel.onEmailUpdated(it, binding.passwordInput.text)
        }
        binding.checkPrivacy.setAccessibilityBasedOnViewStateSelection(binding.checkPrivacy.isChecked)
        binding.checkPrivacy.setOnClickListener { it.setAccessibilityBasedOnViewStateSelection(binding.checkPrivacy.isChecked) }

        binding.checkPrivacy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showErrorPrivacy(isChecked)
            else {
                binding.checkPrivacy.buttonTintList = ColorStateList.valueOf(getColor(R.color.onSurface))
                binding.privacyErrorHint.setVisible(false)
                binding.privacyErrorIcon.setVisible(false)
            }
            binding.checkPrivacy.requestFocus()
        }

        binding.registerBtn.setOnClickListener {
            binding.contentLayout.requestFocus()
            showErrorPrivacy(binding.checkPrivacy.isChecked)
            if (!binding.checkPrivacy.isChecked) return@setOnClickListener
            if (!areFieldsErrorFree() && !areFieldsFilled()) return@setOnClickListener
            binding.registerBtn.startLoading()
            viewModel.onRegisterButtonClicked(
                binding.emailInput.text,
                binding.passwordInput.text,
                binding.birthdayInput.text,
                binding.postCodeInput.text
            )
        }
        setBehaviorListeners()
    }

    private fun setBehaviorListeners() {
        val textWatcher = object : EmptyTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                updateButtonStatus()
            }
        }

        binding.emailInput.editText.addTextChangedListener(textWatcher)
        binding.passwordInput.editText.addTextChangedListener(textWatcher)
        binding.repeatPasswordInput.editText.addTextChangedListener(textWatcher)
        binding.postCodeInput.editText.addTextChangedListener(textWatcher)
    }

    private fun initPasswordViews() {
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

        binding.postCodeInput.onFocusChanged { focused ->
            if (!focused) viewModel.onPostCodeReady(binding.postCodeInput.text)
        }

        binding.repeatPasswordInput.onTextChanged {
            if (it.isNotBlank()) {
                viewModel.onSecondPasswordTextChanged(binding.passwordInput.text to binding.repeatPasswordInput.text)
            } else {
                binding.repeatPasswordInput.validation = FieldValidation(FieldValidation.IDLE, null)
            }
        }
    }

    private fun initBirthdayView() {
        binding.birthdayInput.setOnClickListener {
            DialogUtil.showDatePickerDialog(
                fragmentManager = childFragmentManager,
                onDateSelected = { date ->
                    if (date.toDateString() != binding.birthdayInput.text) binding.birthdayInput.text =
                        date.toDateString()
                    binding.birthdayInput.validation = FieldValidation(FieldValidation.IDLE, null)
                    binding.postCodeInput.requestFocusAtEnd()
                    updateButtonStatus()
                },
                onCancel = { binding.birthdayInput.clear() }
            )
        }
    }

    private fun subscribeUi() {
        subscribePasswordValidation()
        subscribeRegistration()
        subscribeErrorHandling()
    }

    private fun subscribePasswordValidation() {
        viewModel.passwordStrength.observe(viewLifecycleOwner) {
            binding.passwordStrength.updateValidation(it)
            if (it.percentage == 100) {
                binding.repeatPasswordInput.activate()
            } else {
                binding.repeatPasswordInput.deactivate()
            }
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) {
            regFields[it.first]?.validation = it.second
            updateButtonStatus()
        }
    }

    private fun subscribeRegistration() {
        viewModel.registration.observe(viewLifecycleOwner) {
            binding.postCodeInput.validation = it
            lifecycleScope.launchWhenResumed {
                delay(3000)
                findNavController().navigate(
                    R.id.action_registration_to_registration_confirmation_graph,
                    RegistrationConfirmationGraphArgs.Builder(
                        binding.birthdayInput.text,
                        binding.postCodeInput.text,
                        binding.emailInput.text,
                        VerificationSuccess.REGISTRATION_CONFIRMED,
                        PINVerificationActions.REGISTRATION_RESEND,
                        PINVerificationActions.REGISTRATION_VALIDATION,
                        R.string.r_004_registration_confirmation_title,
                        R.string.r_004_registration_confirmation_info_enter_pin,
                        R.string.r_004_registration_confirmation_info_sent_mail,
                        null
                    ).build().toBundle()
                )
            }
        }
        viewModel.resendTooSoon.observe(viewLifecycleOwner) {
            findNavController().navigate(
                R.id.action_registration_to_registration_confirmation_graph,
                RegistrationConfirmationGraphArgs.Builder(
                    binding.birthdayInput.text,
                    binding.postCodeInput.text,
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
        viewModel.validatedOkFields.observe(viewLifecycleOwner) { showOkOnUnvalidatedFields() }

        viewModel.stopLoading.observe(viewLifecycleOwner) { binding.registerBtn.stopLoadingAfterError() }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.registerBtn.stopLoading()
            DialogUtil.showInfoDialog(
                requireContext(),
                R.string.r_004_registration_confirmation_dialog_failed_title,
                R.string.r_004_registration_confirmation_dialog_failed_content
            )
        }
    }

    private fun showOkOnUnvalidatedFields() {
        if (binding.emailInput.validation.state == FieldValidation.IDLE) {
            binding.emailInput.validation = FieldValidation(FieldValidation.OK, null)
        }
        if (binding.birthdayInput.validation.state == FieldValidation.IDLE) {
            binding.birthdayInput.validation = FieldValidation(FieldValidation.OK, null)
        }
        if (binding.postCodeInput.validation.state == FieldValidation.IDLE) {
            binding.postCodeInput.validation = FieldValidation(FieldValidation.OK, null)
        }
        binding.privacyErrorIcon.setImageResource(R.drawable.ic_icon_val_ok)
        binding.privacyErrorIcon.setVisible(true)
    }

    private fun subscribeErrorHandling() {
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            binding.registerBtn.stopLoading()
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = {
                    binding.registerBtn.startLoading()
                    viewModel.onRetryRequired()
                },
                onCancel = viewModel::onRetryCanceled
            )
        }
    }

    private fun showErrorPrivacy(isChecked: Boolean) {
        val colorId = if (isChecked) R.color.onSurface else R.color.red
        binding.checkPrivacy.buttonTintList = ColorStateList.valueOf(getColor(colorId))
        binding.privacyErrorIcon.setImageResource(R.drawable.ic_icon_val_error)
        binding.privacyErrorHint.setVisible(!isChecked)
        binding.privacyErrorIcon.setVisible(!isChecked)
    }

    private fun updateButtonStatus() {
        if (areFieldsErrorFree() && areFieldsFilled()) {
            binding.registerBtn.enable()
        } else {
            binding.registerBtn.disable()
        }
    }

    private fun areFieldsFilled() = binding.emailInput.text.isNotBlank() &&
            binding.passwordInput.text.isNotBlank() &&
            binding.repeatPasswordInput.text.isNotBlank() &&
            binding.postCodeInput.text.isNotBlank()

    private fun areFieldsErrorFree() = !binding.emailInput.hasErrors &&
            !binding.passwordInput.hasErrors &&
            !binding.repeatPasswordInput.hasErrors &&
            !binding.postCodeInput.hasErrors
}
