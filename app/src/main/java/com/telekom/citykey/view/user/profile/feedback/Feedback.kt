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

package com.telekom.citykey.view.user.profile.feedback

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.databinding.FeedbackDialogBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.EmptyTextWatcher
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class Feedback : FullScreenBottomSheetDialogFragment(R.layout.feedback_dialog) {
    private val binding: FeedbackDialogBinding by viewBinding(FeedbackDialogBinding::bind)
    private val viewModel: FeedbackViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
    }

    fun subscribeUi() {
        viewModel.feedbackSubmitted.observe(viewLifecycleOwner) {
            binding.feedbackForm.visibility = View.GONE
            binding.feedbackSuccess.visibility = View.VISIBLE
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.sendBtn.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            binding.sendBtn.stopLoading()
            DialogUtil.showRetryDialog(requireContext())
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) {
            if (it.second.stringRes != 0 && binding.contactEmailSwitch.isChecked) {
                binding.contactEmailInput.validation = it.second
            }
            updateButtonStatus()
        }

        viewModel.profileContent.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                binding.contactEmailInput.text = it
        }
    }

    fun initViews() {
        binding.sendBtn.disable()
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        binding.sendBtn.setOnClickListener {
            binding.sendBtn.startLoading()
            val contactEmailId = if (binding.contactEmailSwitch.isChecked) binding.contactEmailInput.text else ""
            viewModel.onSendClicked(
                binding.feedBackTextInput.text.toString(),
                binding.feedbackTextInput1.text.toString(),
                contactEmailId
            )
        }
        binding.okButton.setOnClickListener {
            dismiss()
        }

        binding.contactEmailInput.onTextChanged { text ->
            if (binding.contactEmailSwitch.isChecked)
                viewModel.onEmailReady(text)
        }

        binding.contactEmailSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && binding.contactEmailInput.text.isNotEmpty()) {
                viewModel.onEmailReady(binding.contactEmailInput.text)
            } else {
                binding.contactEmailInput.validation = FieldValidation(FieldValidation.IDLE, null)
            }
            binding.contactEmailInput.isEnabled = isChecked
            updateButtonStatus()
        }

        setBehaviorListeners()
    }

    private fun setBehaviorListeners() {
        val textWatcher = object : EmptyTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                updateButtonStatus()
            }
        }
        binding.feedBackTextInput.addTextChangedListener(textWatcher)
        binding.feedbackTextInput1.addTextChangedListener(textWatcher)
    }

    private fun updateButtonStatus() {
        var isUserEmailEnable =
            binding.contactEmailSwitch.isChecked && binding.contactEmailInput.text.isNotEmpty() && !binding.contactEmailInput.hasErrors

        if ((binding.feedBackTextInput.text?.isNotBlank() == true || binding.feedbackTextInput1.text?.isNotEmpty() == true) && ((isUserEmailEnable) || !binding.contactEmailSwitch.isChecked)) {
            binding.sendBtn.enable()
        } else {
            binding.sendBtn.disable()
        }
    }
}
