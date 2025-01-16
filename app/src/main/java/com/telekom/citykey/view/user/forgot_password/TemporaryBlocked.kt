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

package com.telekom.citykey.view.user.forgot_password

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.view.user.login.LoginActivity

class TemporaryBlocked : Fragment(R.layout.temporary_blocked_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as LoginActivity).run {
            adaptToolbarForBack()
            setTopIcon(R.drawable.ic_icon_account_locked)
        }

        view.findViewById<View>(R.id.resetPasswordBtn).setOnClickListener {
            findNavController().navigate(
                TemporaryBlockedDirections.actionTemporaryBlockedToForgotPassword2(
                    arguments?.getString("email")
                )
            )
        }
        view.findViewById<View>(R.id.resetPasswordBtn).setAccessibilityRole(AccessibilityRole.Button)
    }
}
