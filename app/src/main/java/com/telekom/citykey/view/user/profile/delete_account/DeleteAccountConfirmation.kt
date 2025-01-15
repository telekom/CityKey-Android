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

package com.telekom.citykey.view.user.profile.delete_account

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileDeleteAccountConfirmationBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions

class DeleteAccountConfirmation : Fragment(R.layout.profile_delete_account_confirmation) {
    private val binding by viewBinding(ProfileDeleteAccountConfirmationBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as ProfileActivity).run {
            setPageTitle(R.string.d_003_delete_account_confirmation_title)
            adaptToolbarForClose()
            backAction = ProfileBackActions.FINISH
        }

        binding.okButton.setOnClickListener {
            activity?.finish()
        }
    }
}
