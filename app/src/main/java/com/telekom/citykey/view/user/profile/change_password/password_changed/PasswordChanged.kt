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

package com.telekom.citykey.view.user.profile.change_password.password_changed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfilePasswordChangedFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions

class PasswordChanged : Fragment(R.layout.profile_password_changed_fragment) {
    private val binding by viewBinding(ProfilePasswordChangedFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as ProfileActivity).run {
            adaptToolbarForClose()
            setPageTitle(R.string.p_006_profile_password_changed_title)
            backAction = ProfileBackActions.LOGOUT
        }

        binding.loginButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
