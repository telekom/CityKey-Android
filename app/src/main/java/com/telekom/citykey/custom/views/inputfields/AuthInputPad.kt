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

package com.telekom.citykey.custom.views.inputfields

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AuthInputPadBinding

class AuthInputPad @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val binding: AuthInputPadBinding =
        AuthInputPadBinding.inflate(LayoutInflater.from(context), this, true)

    fun attachToPinInput(view: PinInputLayout) {
        val padListener: (View) -> Unit = {
            view.addChar((it as TextView).text[0])
        }

        binding.numOne.setOnClickListener(padListener)
        binding.numTwo.setOnClickListener(padListener)
        binding.numThree.setOnClickListener(padListener)
        binding.numFour.setOnClickListener(padListener)
        binding.numFive.setOnClickListener(padListener)
        binding.numSix.setOnClickListener(padListener)
        binding.numSeven.setOnClickListener(padListener)
        binding.numEight.setOnClickListener(padListener)
        binding.numNine.setOnClickListener(padListener)
        binding.numZero.setOnClickListener(padListener)

        binding.numDelete.setOnClickListener {
            view.removeLastChar()
        }

        binding.numTogglePass.setOnClickListener {
            view.toggleHideContent()
            binding.numTogglePass.setImageResource(
                if (view.isContentHidden)
                    R.drawable.ic_auth_input_toggle_password_off
                else
                    R.drawable.ic_auth_input_toggle_password_on
            )
        }
    }
}
