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

package com.telekom.citykey.view.user.pin_verification

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PinVerificationFragmentBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.track.AnalyticsParameterKey
import com.telekom.citykey.utils.DateUtil
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.registration.RegistrationActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PINVerification : Fragment(R.layout.pin_verification_fragment) {
    private val viewModel: PINVerificationViewModel by viewModel()
    private val args: PINVerificationArgs by navArgs()
    private val binding by viewBinding(PinVerificationFragmentBinding::bind)

    private val adjustManager: AdjustManager by inject()

    private var resendInfoAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contentParent.layoutTransition =
            LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }

        when (val activity = requireActivity()) {
            is RegistrationActivity -> {
                activity.setToolbarTitle(R.string.r_004_registration_confirmation_title)
                activity.setTopIcon(R.drawable.ic_icon_confirm_email)
            }
            is LoginActivity -> {
                activity.adaptToolbarForBack()
                activity.setPageTitle(args.title)
                activity.setTopIcon(R.drawable.ic_icon_confirm_email)
            }
            is ProfileActivity -> {
                activity.setPageTitle(args.title)
            }
        }

        binding.labelPINInfo.setText(args.helperText)

        initViews()
        subscribeUi()
    }

    private fun initViews() {
        binding.pinConfirmedInfo.text = getString(args.headlineTextFormat, args.email.bold()).decodeHTML()

        binding.pinInputView.onTextChanged {
            if (binding.pinInputView.text.length == binding.pinInputView.maxLength) {
                viewModel.onPinConfirmed(
                    binding.pinInputView.text,
                    args.email,
                    args.validationAction,
                    args.actionType
                )
            }
        }

        binding.resendButton.setOnClickListener {
            viewModel.onResendEmailClicked(args.email, args.resendAction)
            binding.pinInputView.clear()
        }

        if (!args.errorText.isNullOrBlank()) {
            binding.resendInfo.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            binding.labelSentPasswordChangeMailInfo.setTextColor(getColor(R.color.red))
            binding.labelSentPasswordChangeMailInfo.text = args.errorText
        }
        binding.resendButton.setAccessibilityRole(AccessibilityRole.Button)
    }

    private fun subscribeUi() {
        viewModel.emailResent.observe(viewLifecycleOwner) {
            if (it) revealResendInfo(
                getString(R.string.r_004_registration_confirmation_info_sent_link),
                getColor(R.color.infoGreen)
            )
        }
        viewModel.resendError.observe(viewLifecycleOwner) {
            revealResendInfo(it, getColor(R.color.red))
        }
        viewModel.confirmError.observe(viewLifecycleOwner) {
            binding.pinInputView.errorText = it
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
        }
        viewModel.pinConfirmed.observe(viewLifecycleOwner) {
            adjustManager.trackEvent(
                R.string.registration_complete,
                mapOf(
                    AnalyticsParameterKey.userYearOfBirth to DateUtil.stringToDate(args.dob).year(),
                    AnalyticsParameterKey.userZipCode to args.zipcode
                )
            )
            findNavController().navigate(
                PINVerificationDirections.actionRegistrationConfirmationToRegistrationSuccess(
                    args.dob,
                    args.zipcode,
                    it
                )
            )
        }
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                requireContext(),
                viewModel::onRetryRequired
            ) {
                viewModel.onRetryCanceled()
            }
        }
    }

    private fun revealResendInfo(text: String, @ColorInt textColor: Int) {
        binding.labelSentPasswordChangeMailInfo.setTextColor(textColor)
        binding.labelSentPasswordChangeMailInfo.text = text

        resendInfoAnimator = ValueAnimator.ofInt(0, measuredResendInfoHeight).apply {
            duration = 300
            addUpdateListener {
                binding.resendInfo.layoutParams.height = it.animatedValue as Int
                binding.resendInfo.requestLayout()
            }
            start()
        }
    }

    private val measuredResendInfoHeight
        get() = binding.resendInfo.apply {
            measure(
                View.MeasureSpec.makeMeasureSpec(
                    binding.contentParent.width, View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                    binding.contentParent.height, View.MeasureSpec.AT_MOST
                )
            )
        }.measuredHeight
}
