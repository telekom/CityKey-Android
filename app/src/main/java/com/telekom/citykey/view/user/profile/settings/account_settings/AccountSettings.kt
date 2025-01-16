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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.user.profile.settings.account_settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileSettingsFragmentBinding
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions

class AccountSettings : Fragment(R.layout.profile_settings_fragment) {

    private val binding by viewBinding(ProfileSettingsFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as ProfileActivity).run {
            setPageTitle(R.string.p_001_profile_label_account_settings)
            adaptToolbarForBack()
            backAction = ProfileBackActions.BACK
        }
        initViews()
    }

    private fun initViews() {
        setAccessibilityRole()
        binding.userEmailText.text = arguments?.getString("email")

        binding.changeEmailButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_accountSettings_to_changeEmail)
        }

        binding.changePasswordButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_accountSettings_to_changePassword)
        }
        binding.deleteAccountButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_accountSettings_to_deleteAccountInformation)
        }
    }

    fun setAccessibilityRole() {
        binding.changeEmailButton.setAccessibilityRole(AccessibilityRole.Button)
        binding.changePasswordButton.setAccessibilityRole(AccessibilityRole.Button)
        binding.deleteAccountButton.setAccessibilityRole(AccessibilityRole.Button)
    }
}
