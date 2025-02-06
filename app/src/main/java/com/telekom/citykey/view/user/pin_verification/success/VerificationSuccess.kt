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

package com.telekom.citykey.view.user.pin_verification.success

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.RegistrationSuccessFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions
import com.telekom.citykey.view.user.registration.RegistrationActivity

class VerificationSuccess : Fragment(R.layout.registration_success_fragment) {
    private val args: VerificationSuccessArgs by navArgs()
    private val binding by viewBinding(RegistrationSuccessFragmentBinding::bind)

    companion object {
        const val REGISTRATION_CONFIRMED = 0
        const val PASSWORD_CHANGED = 1
        const val EMAIL_CHANGED = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.resultType == EMAIL_CHANGED) {
            binding.imgSuccess.visibility = View.VISIBLE
        }
        when (val activity = requireActivity()) {
            is RegistrationActivity -> {
                activity.setTopIcon(R.drawable.ic_icon_account_success)
                activity.setToolbarTitle(R.string.r_005_registration_success_title)
            }
            is LoginActivity -> {
                activity.adaptToolbarForClose()
                activity.setPageTitle(
                    if (args.resultType == REGISTRATION_CONFIRMED) R.string.r_005_registration_success_title
                    else R.string.f_001_forgot_password_title
                )
                activity.setTopIcon(R.drawable.ic_icon_account_success)
                activity.backAction = { findNavController().popBackStack(R.id.login, false) }
            }
            is ProfileActivity -> {
                activity.adaptToolbarForClose()
                if (args.resultType == EMAIL_CHANGED) {
                    binding.imgSuccess.visibility = View.VISIBLE
                    activity.setPageTitle(R.string.p_004_profile_email_changed_title)
                } else {
                    activity.setPageTitle(R.string.f_001_forgot_password_title)
                }
                activity.backAction = ProfileBackActions.LOGOUT
            }
        }

        binding.pinConfirmedInfo.setText(
            when (args.resultType) {
                REGISTRATION_CONFIRMED -> {
                    R.string.r_005_registration_success_headline
                }
                PASSWORD_CHANGED -> {
                    R.string.f_003_forgot_password_confirm_success_headline
                }
                else -> {
                    R.string.p_004_profile_email_changed_info_sent_mail
                }
            }
        )

        binding.pinConfirmedAdditionalInfo.setText(
            when (args.resultType) {
                REGISTRATION_CONFIRMED -> {
                    R.string.r_005_registration_success_details
                }
                PASSWORD_CHANGED -> {
                    R.string.f_003_forgot_password_confirm_success_details
                }
                else -> {
                    R.string.p_004_profile_email_changed_info_received
                }
            }

        )

        binding.btnLogin.setOnClickListener {
            if (requireActivity().intent.getBooleanExtra(LoginActivity.LAUNCH_INFOBOX, false))
                requireActivity().finish() else {
                val activity = requireActivity()
                if (activity is RegistrationActivity) activity.isRegistrationFinished = true
                activity.onBackPressed()
            }
        }
    }
}
