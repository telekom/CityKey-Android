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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PinInputViewBinding
import com.telekom.citykey.utils.extensions.textWatcher

@SuppressLint("UseCompatLoadingForDrawables")
class PinInputLayout(context: Context, attrs: AttributeSet) : TextInputLayout(context, attrs) {
    companion object {
        const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }

    private val binding: PinInputViewBinding = PinInputViewBinding.inflate(LayoutInflater.from(context))

    private var _onTextChanged: ((String) -> Unit)? = null
    private val errorIcon: Drawable by lazy { context.getDrawable(R.drawable.ic_icon_val_error)!! }

    val text get() = binding.pinInput.text!!.toString()
    val maxLength get() = binding.pinInput.maxLength
    val isContentHidden get() = binding.pinInput.isHideContent

    var errorText: String? = null
        set(value) {
            if (field == value) return
            field = value
            binding.pinInput.hasError = value != null
            binding.pinInputLayout.error = errorText
            binding.pinInputLayout.endIconMode = if (value == null) END_ICON_NONE else END_ICON_CUSTOM
            binding.pinInputLayout.endIconDrawable = errorIcon
            binding.pinInputLayout.setErrorTextAppearance(R.style.PinErrorText)
            binding.pinInputLayout.findViewById<TextView>(R.id.textinput_error)
                .textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

    init {
        addView(binding.root)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PinInputLayout,
            0, 0
        ).apply {

            try {
                binding.pinInput.isHideContent = getBoolean(R.styleable.PinInputLayout_hideContent, false)
                val maxLength = getInteger(R.styleable.PinInputLayout_pinSize, 6)
                binding.pinInput.maxLength = maxLength
                binding.pinInput.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
                binding.pinInputLayout.setErrorTextAppearance(R.style.stateText)
            } finally {
                recycle()
            }
        }

        binding.pinInput.isEnabled = attrs.getAttributeBooleanValue(XML_NAMESPACE_ANDROID, "enabled", true)

        setup()
    }

    private fun setup() {
        binding.pinInput.textWatcher {
            onTextChanged { chars, _, _, _ ->
                _onTextChanged?.invoke(chars.toString())
                errorText = null
            }
        }
    }

    fun toggleHideContent() {
        binding.pinInput.isHideContent = binding.pinInput.isHideContent.not()
    }

    fun onTextChanged(listener: ((String) -> Unit)) {
        _onTextChanged = listener
    }

    @SuppressLint("SetTextI18n")
    fun addChar(char: Char) {
        val currentText = binding.pinInput.text.toString()

        if (binding.pinInput.maxLength == currentText.length) return

        binding.pinInput.setText(currentText + char)
    }

    fun removeLastChar() {
        val currentText = binding.pinInput.text.toString()

        if (currentText.isEmpty()) return

        binding.pinInput.setText(currentText.substring(0, currentText.length - 1))
    }

    fun clear() {
        errorText = null
        binding.pinInput.setText("")
    }
}
