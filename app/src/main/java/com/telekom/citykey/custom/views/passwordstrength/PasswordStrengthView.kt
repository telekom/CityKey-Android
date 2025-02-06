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

package com.telekom.citykey.custom.views.passwordstrength

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.telekom.citykey.R

class PasswordStrengthView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    companion object {
        private const val BLACK_COLOR = R.color.onSurface
        private const val RED_COLOR = R.color.red
        private const val GREEN_COLOR = R.color.lima
    }

    private val strengthProgressBar: ProgressBar by lazy { findViewById(R.id.strengthProgressbar) }
    private val tvHints: TextView by lazy { findViewById(R.id.tvHints) }
    private val contentHeight: Int
    private var isItemShown = false
    private var heightAnimator: ValueAnimator? = null
    private val view: View = View.inflate(context, R.layout.registration_profile_custom_password_strength_view, null)
    private var lastValidation: PasswordStrength? = null

    init {
        view.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        contentHeight = view.measuredHeight

        addView(view)
    }

    fun toggleCollapsing() {
        heightAnimator?.cancel()

        val toHeight = if (isItemShown) 0 else contentHeight
        isItemShown = !isItemShown

        heightAnimator = ValueAnimator.ofInt(this@PasswordStrengthView.height, toHeight).apply {
            duration = 300
            addUpdateListener {
                this@PasswordStrengthView.layoutParams.height = it.animatedValue as Int
                this@PasswordStrengthView.requestLayout()
            }
            start()
        }
    }

    fun updateValidation(passwordStrength: PasswordStrength) {
        lastValidation = passwordStrength
        updateHints(passwordStrength.spannableStringBuilder)
        setProgress(passwordStrength.percentage)
    }

    fun initValidation(hints: String) {
        tvHints.text = hints
    }

    private fun setProgress(percentage: Int) {
        strengthProgressBar.progressTintList = ColorStateList.valueOf(
            context.getColor(
                when (percentage) {
                    0 -> BLACK_COLOR
                    100 -> GREEN_COLOR
                    else -> RED_COLOR
                }
            )
        )
        strengthProgressBar.progress = percentage
    }

    private fun updateHints(spannableString: SpannableStringBuilder) {
        tvHints.text = spannableString
    }
}
