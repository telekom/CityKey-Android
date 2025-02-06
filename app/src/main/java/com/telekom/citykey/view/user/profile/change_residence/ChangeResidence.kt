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

package com.telekom.citykey.view.user.profile.change_residence

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.databinding.ProfileChangeResidenceFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeResidence : Fragment(R.layout.profile_change_residence_fragment) {

    private val binding by viewBinding(ProfileChangeResidenceFragmentBinding::bind)
    private val viewModel: ChangeResidenceViewModel by viewModel()
    private val args: ChangeResidenceArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? ProfileActivity)?.run {
            setPageTitle(R.string.p_003_profile_new_postcode_title)
            backAction = ProfileBackActions.BACK
            adaptToolbarForBack()
        }
        subscribeUi()
        initViews()
    }

    fun initViews() {
        binding.newPostcodeInput.onTextChanged {
            handleProgressButton()
        }
        binding.saveResidenceBtn.disable()
        binding.oldResidenceInput.text = args.postalCode
        binding.oldResidenceInput.disable()
        binding.saveResidenceBtn.setOnClickListener {
            binding.saveResidenceBtn.startLoading()
            viewModel.onSaveClicked(binding.newPostcodeInput.text)
        }
    }

    fun subscribeUi() {
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = {
                    binding.saveResidenceBtn.startLoading()
                    viewModel.onRetryRequired()
                },
                onCancel = viewModel::onRetryCanceled
            )
            binding.saveResidenceBtn.stopLoading()
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.saveResidenceBtn.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.onlineErrors.observe(viewLifecycleOwner) { errorMessage ->
            binding.saveResidenceBtn.stopLoading()
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.ERROR, errorMessage.first)
            handleProgressButton()
        }

        viewModel.generalErrors.observe(viewLifecycleOwner) {
            binding.saveResidenceBtn.stopLoading()
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.ERROR, null, it)
        }

        viewModel.logUserOut.observe(viewLifecycleOwner) {
            (activity as ProfileActivity).logOut()
        }

        viewModel.saveSuccessful.observe(viewLifecycleOwner) {
            binding.saveResidenceBtn.stopLoading()
            findNavController().navigateUp()
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) { error ->
            binding.saveResidenceBtn.stopLoading()
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.ERROR, null, error)
            handleProgressButton()
        }

        viewModel.requestSent.observe(viewLifecycleOwner) {
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.SUCCESS, it.postalCodeMessage)
        }
    }

    private fun handleProgressButton() {
        if (!binding.newPostcodeInput.hasErrors && binding.newPostcodeInput.text.isNotBlank()) {
            binding.saveResidenceBtn.enable()
        } else {
            binding.saveResidenceBtn.disable()
        }
    }
}
