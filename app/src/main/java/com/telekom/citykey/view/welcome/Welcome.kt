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

package com.telekom.citykey.view.welcome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomePageFragmentBinding
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.registration.RegistrationActivity

class Welcome : Fragment(R.layout.welcome_page_fragment) {
    private val binding by viewBinding(WelcomePageFragmentBinding::bind)

    companion object {
        const val RESULT_CODE_LOGIN_TO_REGISTRATION = 103
        const val RESULT_CODE_REGISTRATION_TO_LOGIN = 104
    }

    private val loginARL: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_CODE_LOGIN_TO_REGISTRATION -> {
                    registerARL.launch(
                        Intent(activity, RegistrationActivity::class.java).apply {
                            putExtra("isFirstTime", true)
                            putExtra("isLaunchedByLogin", true)
                        }
                    )
                }

                Activity.RESULT_OK -> {
                    requireActivity().finish()
                    startActivity<MainActivity>()
                }
            }
        }

    private val registerARL: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_CODE_REGISTRATION_TO_LOGIN)
                loginARL.launch(
                    Intent(activity, LoginActivity::class.java).apply {
                        putExtra("isFirstTime", true)
                    }
                )
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun initViews() {
        binding.loginLink.setAccessibilityRole(AccessibilityRole.Button)
        binding.descHeading.setAccessibilityRole(AccessibilityRole.Heading)
        binding.loginLink.setOnClickListener {
            loginARL.launch(
                Intent(activity, LoginActivity::class.java).apply {
                    putExtra("isFirstTime", true)
                }
            )
        }

        binding.registerBtn.setOnClickListener {
            registerARL.launch(
                Intent(activity, RegistrationActivity::class.java).apply {
                    putExtra("isFirstTime", true)
                }
            )
        }
    }
}
