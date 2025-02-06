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

package com.telekom.citykey.view.services.citizen_surveys.data_privacy

import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyDataPrivacyFragmentBinding
import com.telekom.citykey.utils.extensions.decodeHTML
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment

class DataPrivacyNotice(
    private val surveyDataPrivacy: String,
    private val cityColor: Int,
    private val resultListener: (Boolean) -> Unit,
) : FullScreenBottomSheetDialogFragment(R.layout.survey_data_privacy_fragment) {

    private val binding by viewBinding(SurveyDataPrivacyFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarDataPrivacy.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarDataPrivacy.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarDataPrivacy.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbarDataPrivacy.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbarDataPrivacy)
        binding.btnDataPrivacy.setupNormalStyle(cityColor)
        binding.dataPrivacyContent.text = surveyDataPrivacy.decodeHTML()
        binding.btnDataPrivacy.setOnClickListener {
            resultListener.invoke(true)
            dismiss()
        }
    }
}
