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

package com.telekom.citykey.view.user.login.login

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.credentials.Credential
import com.telekom.citykey.R
import com.telekom.citykey.RegistrationConfirmationGraphArgs
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.databinding.LoginFragmentBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.track.AnalyticsParameterKey
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.EmptyTextWatcher
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.setAccessibilityBasedOnViewStateSelection
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.updateWasteCalendarWidget
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.extensions.year
import com.telekom.citykey.view.dialogs.AccessibilityDisclaimerDialog
import com.telekom.citykey.view.dialogs.DataPrivacyNoticeDialog
import com.telekom.citykey.view.dialogs.DataPrivacySettingsDialog
import com.telekom.citykey.view.dialogs.HelpSectionDialog
import com.telekom.citykey.view.dialogs.ImprintBottomSheetDialog
import com.telekom.citykey.view.dialogs.SoftwareLicenseDialog
import com.telekom.citykey.view.dialogs.dpn_updates.DpnUpdatesDialog
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.pin_verification.PINVerificationActions
import com.telekom.citykey.view.user.pin_verification.success.VerificationSuccess
import com.telekom.citykey.view.user.profile.feedback.Feedback
import com.telekom.citykey.view.user.registration.RegistrationActivity
import com.telekom.citykey.view.welcome.WelcomeActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date

class Login : Fragment(R.layout.login_fragment) {

    private val viewModel: LoginViewModel by viewModel()
    private val binding by viewBinding(LoginFragmentBinding::bind)
    private val adjust: AdjustManager by inject()

    private val credentialsResolutionARL: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val credentials = result.data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    credentials?.let {
                        binding.loginEmailInput.text = it.id
                        binding.loginPasswordInput.text = it.password ?: ""
                    }
                }
            }
        }

    private val credentialsResolutionARLSave: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    (activity as LoginActivity).finishOrOpenProfile()
                }

                Activity.RESULT_CANCELED -> {
                    (activity as LoginActivity).finishOrOpenProfile()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as LoginActivity).run {
            adaptToolbarForClose()
            setTopIcon(R.drawable.ic_icon_login)
            setPageTitle(R.string.l_001_login_title)
        }

        initViews()
        subscribeUi()
    }

    private fun initViews() {
        binding.progressBtnLogin.disable()
        setAccessibilityRoles()
        val textWatcher = object : EmptyTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                binding.loginEmailInput.validation = FieldValidation(FieldValidation.IDLE, null)
                binding.loginPasswordInput.validation = FieldValidation(FieldValidation.IDLE, null)

                if (binding.loginEmailInput.text.isBlank() || binding.loginPasswordInput.text.isBlank()) binding.progressBtnLogin.disable()
                else binding.progressBtnLogin.enable()
            }
        }
        binding.feedbackButton.setOnClickListener {
            Feedback()
                .showDialog(childFragmentManager)
        }
        binding.loginEmailInput.editText.addTextChangedListener(textWatcher)
        binding.loginPasswordInput.editText.addTextChangedListener(textWatcher)

        binding.progressBtnLogin.setOnClickListener {
            binding.progressBtnLogin.startLoading()
            binding.loginEmailInput.validation = FieldValidation(FieldValidation.IDLE, null)
            binding.loginPasswordInput.validation = FieldValidation(FieldValidation.IDLE, null)

            if (!binding.checkboxStayLoggedin.isChecked)
                DialogUtil.showDialogPositiveNegative(
                    context = requireContext(),
                    title = R.string.l_001_login_kmli_dialog_title,
                    positiveBtnLabel = R.string.positive_button_dialog,
                    negativeBtnLabel = R.string.negative_button_dialog,
                    message = R.string.l_001_login_kmli_dialog_message,
                    positiveClickListener = {
                        binding.checkboxStayLoggedin.isChecked = true
                        onProgressBtnClicked()
                    },
                    negativeClickListener = {
                        onProgressBtnClicked()
                    },
                    isCancelable = false
                )
            else onProgressBtnClicked()
        }

        binding.registerHereLink.setOnClickListener {

            if (requireActivity().intent.getBooleanExtra("isFirstTime", false)) {
                requireActivity().setResult(WelcomeActivity.RESULT_CODE_LOGIN_TO_REGISTRATION)
                requireActivity().finish()
            } else if (requireActivity().intent.getBooleanExtra(LoginActivity.LAUNCH_INFOBOX, false)) {
                startActivity(
                    Intent(requireContext(), RegistrationActivity::class.java).apply {
                        putExtra(LoginActivity.LAUNCH_INFOBOX, true)
                    }
                )
            } else {
                startActivity(
                    Intent(requireContext(), RegistrationActivity::class.java).apply {
                        putExtra("isLaunchedByLogin", true)
                    }
                )
                requireActivity().finish()
            }
        }

        binding.forgotPassword.setOnClickListener {
            it.findNavController()
                .navigate(LoginDirections.actionLoginToForgotPassword(binding.loginEmailInput.text))
        }

        binding.privacyBtn.setOnClickListener {
            DataPrivacyNoticeDialog().showDialog(childFragmentManager)
        }

        binding.imprintButton.setOnClickListener {
            ImprintBottomSheetDialog()
                .showDialog(childFragmentManager)
        }
        binding.privacySettingsBtn.setOnClickListener {
            DataPrivacySettingsDialog()
                .showDialog(childFragmentManager)
        }

        binding.helpLinkContainer.setOnClickListener { HelpSectionDialog().showDialog(childFragmentManager) }
        binding.disclaimerButton.setOnClickListener { AccessibilityDisclaimerDialog().showDialog(childFragmentManager) }
        binding.softwareLicense.setOnClickListener { SoftwareLicenseDialog().showDialog(childFragmentManager) }
        binding.checkboxStayLoggedin.setAccessibilityBasedOnViewStateSelection(binding.checkboxStayLoggedin.isChecked)
        binding.checkboxStayLoggedin.setOnClickListener { it.setAccessibilityBasedOnViewStateSelection(binding.checkboxStayLoggedin.isChecked) }
    }

    private fun onProgressBtnClicked() {
        viewModel.onLoginBtnPressed(
            binding.loginEmailInput.text,
            binding.loginPasswordInput.text,
            binding.checkboxStayLoggedin.isChecked,
            requireActivity().intent.getBooleanExtra("isFirstTime", false)
        )
    }

    private fun subscribeUi() {
        viewModel.loginHint.observe(viewLifecycleOwner, ::setLoginHint)
        viewModel.login.observe(viewLifecycleOwner) { loginSuccessful ->
            adjust.trackEvent(if (binding.checkboxStayLoggedin.isChecked) R.string.login_with_keep else R.string.login_without_keep)
            if (loginSuccessful) {
                (activity as LoginActivity).finishOrOpenProfile()
                AppWidgetManager.getInstance(requireContext()).updateWasteCalendarWidget(requireContext())
            } else {
                binding.progressBtnLogin.stopLoading()
            }
        }

        viewModel.userProfile.observe(viewLifecycleOwner) {
            val yearOfBirth = it.dateOfBirth?.year() ?: Date().year()
            adjust.trackEvent(R.string.login_complete, mapOf(AnalyticsParameterKey.userYearOfBirth to yearOfBirth))
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) { showRetryDialog() }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            val message = getString(R.string.dialog_technical_error_message)
            binding.loginEmailInput.validation = FieldValidation(FieldValidation.ERROR, message)
            binding.loginPasswordInput.validation = FieldValidation(FieldValidation.ERROR, message)
            binding.progressBtnLogin.stopLoading()
        }
        viewModel.error.observe(viewLifecycleOwner, Observer(this::displayErrors))
        viewModel.emailNotConfirmed.observe(viewLifecycleOwner) {
            findNavController().navigate(
                R.id.action_login_to_registration_confirmation_graph,
                RegistrationConfirmationGraphArgs.Builder(
                    "", "",
                    binding.loginEmailInput.text,
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
        viewModel.credentials.observe(viewLifecycleOwner) {
            binding.loginEmailInput.text = it.id
            binding.loginPasswordInput.text = it.password!!
        }
        viewModel.resolutions.observe(viewLifecycleOwner) {
            credentialsResolutionARL.launch(IntentSenderRequest.Builder(it).build())
        }
        viewModel.resolutionSave.observe(viewLifecycleOwner) {
            credentialsResolutionARLSave.launch(IntentSenderRequest.Builder(it).build())
        }
        viewModel.showDpnUpdates.observe(viewLifecycleOwner) {
            DpnUpdatesDialog {
                (activity as LoginActivity).finishOrOpenProfile()
            }.showDialog(parentFragmentManager)
        }
    }

    private fun setLoginHint(logoutReason: LogoutReason) {
        when (logoutReason) {
            LogoutReason.ACTIVE_LOGOUT -> {
                binding.loginHint.setText(R.string.l_001_login_hint_active_logout)
                binding.loginHintContainer.setVisible(true)
            }

            LogoutReason.TECHNICAL_LOGOUT -> {
                binding.loginHint.setText(R.string.l_001_login_hint_technical_logout)
                binding.loginHintContainer.setVisible(true)
            }

            LogoutReason.TOKEN_EXPIRED_LOGOUT -> {
                binding.loginHint.setText(R.string.l_001_login_hint_kmli_not_checked)
                binding.loginHintContainer.setVisible(true)
            }

            else -> binding.loginHintContainer.setVisible(false)
        }
    }

    private fun showRetryDialog() {
        binding.progressBtnLogin.stopLoading()
        DialogUtil.showRetryDialog(
            context = requireContext(),
            onCancel = viewModel::onRetryCanceled,
            onRetry = {
                binding.progressBtnLogin.startLoading()
                viewModel.onRetryRequired()
            }
        )
    }

    private fun displayErrors(validation: FieldValidation) {
        binding.loginEmailInput.validation = validation
        binding.loginPasswordInput.validation = validation
        binding.progressBtnLogin.stopLoadingAfterError()
    }

    private fun setAccessibilityRoles() {
        binding.helpSectionHeaderLabel.setAccessibilityRole(AccessibilityRole.Heading,(getString(R.string.accessibility_heading_level_2)))
        binding.imprintButton.setAccessibilityRole(AccessibilityRole.Link)
        binding.privacyBtn.setAccessibilityRole(AccessibilityRole.Link)
        binding.privacySettingsBtn.setAccessibilityRole(AccessibilityRole.Link)
        binding.feedbackButton.setAccessibilityRole(AccessibilityRole.Link)
        binding.disclaimerButton.setAccessibilityRole(AccessibilityRole.Link)
        binding.softwareLicense.setAccessibilityRole(AccessibilityRole.Link)
        binding.registerHereLink.setAccessibilityRole(AccessibilityRole.Button)
        binding.forgotPassword.setAccessibilityRole(AccessibilityRole.Button)
    }
}
