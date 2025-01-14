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
