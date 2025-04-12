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

package com.telekom.citykey.view.user.profile.change_email

import android.os.Bundle
import android.view.View
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.RegistrationConfirmationGraphArgs
import com.telekom.citykey.databinding.ProfileChangeEmailFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.pin_verification.PINVerificationActions
import com.telekom.citykey.view.user.pin_verification.success.VerificationSuccess
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeEmail : Fragment(R.layout.profile_change_email_fragment) {
    private val viewModel: ChangeEmailViewModel by viewModel()
    private val binding by viewBinding(ProfileChangeEmailFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
        initListeners()

        (activity as? ProfileActivity)?.run {
            setPageTitle(R.string.p_003_profile_email_change_title)
            backAction = ProfileBackActions.BACK
            adaptToolbarForBack()
        }
    }

    private fun subscribeUi() {
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = {
                    binding.accessRightsBtn.startLoading()
                    binding.newEmailInput.deactivate()
                    viewModel.onRetryRequired()
                },
                onCancel = viewModel::onRetryCanceled
            )
            binding.accessRightsBtn.stopLoading()
            binding.newEmailInput.activate()
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.accessRightsBtn.stopLoading()
            binding.newEmailInput.activate()
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.requestSent.observe(viewLifecycleOwner) {
            findNavController().navigate(
                R.id.action_changeEmail_to_registration_confirmation_graph2,
                RegistrationConfirmationGraphArgs.Builder(
                    "", "",
                    binding.newEmailInput.text,
                    VerificationSuccess.EMAIL_CHANGED,
                    PINVerificationActions.REGISTRATION_RESEND,
                    PINVerificationActions.REGISTRATION_VALIDATION,
                    R.string.r_004_registration_confirmation_title,
                    R.string.r_004_registration_confirmation_info_enter_pin,
                    R.string.p_005_profile_email_changed_pin_verification_message,
                    null
                ).build().toBundle()
            )
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) { error ->
            if (error != null)
                binding.newEmailInput.error = getString(error)
            else
                binding.newEmailInput.ok = true
            handleProgressButton()
        }

        viewModel.generalErrors.observe(viewLifecycleOwner) {
            binding.accessRightsBtn.stopLoadingAfterError()
            binding.newEmailInput.activate()
            binding.newEmailInput.error = getString(it)
        }
        viewModel.onlineErrors.observe(viewLifecycleOwner) { errorMessage ->
            binding.accessRightsBtn.stopLoadingAfterError()
            binding.newEmailInput.activate()
            binding.newEmailInput.error = errorMessage.first
        }
        viewModel.currentEmail.observe(viewLifecycleOwner) {
            binding.currentEmailInput.text = it
        }
        viewModel.logUserOut.observe(viewLifecycleOwner) {
            (activity as ProfileActivity).logOut()
        }
    }

    private fun handleProgressButton() {
        if (binding.newEmailInput.error == null && binding.newEmailInput.isNotEmpty()) {
            binding.accessRightsBtn.enable()
        } else {
            binding.accessRightsBtn.disable()
        }
    }

    private fun initViews() {
        binding.accessRightsBtn.disable()
    }

    private fun initListeners() {
        binding.accessRightsBtn.setOnClickListener {
            binding.accessRightsBtn.startLoading()
            binding.newEmailInput.deactivate()
            viewModel.onSaveClicked(
                binding.newEmailInput.text,
            )
        }
        binding.newEmailInput.onTextChanged {
            viewModel.onEmailChange(it)
        }
    }
}
