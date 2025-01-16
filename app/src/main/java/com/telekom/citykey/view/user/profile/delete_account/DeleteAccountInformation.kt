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
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileDeleteAccountInformationFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity

class DeleteAccountInformation : Fragment(R.layout.profile_delete_account_information_fragment) {
    private val binding by viewBinding(ProfileDeleteAccountInformationFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as ProfileActivity).run {
            setPageTitle(R.string.d_001_delete_account_info_title)
            adaptToolbarForBack()
        }
        val deleteInfoText =
            "${getString(R.string.d_001_delete_account_info)}<br/><br/>${getString(R.string.d_001_delete_account_info1)}"
        binding.deleteInfo.text = HtmlCompat.fromHtml(deleteInfoText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.deleteAccountButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_deleteAccountInformation_to_deleteAccountValidation)
        }
    }
}
