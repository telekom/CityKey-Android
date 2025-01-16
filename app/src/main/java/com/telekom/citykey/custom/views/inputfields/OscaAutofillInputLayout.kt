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
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.InputFilter
import android.util.AttributeSet
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.RelativeLayout
import com.google.android.material.textfield.TextInputLayout
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.textWatcher

@SuppressLint("UseCompatLoadingForDrawables")
class OscaAutofillInputLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    companion object {
        private const val ERROR_COLOR = 0xFF_FF_5F_5F.toInt()
        private const val OK_COLOR = 0xFF_23_9D_1D.toInt()
    }

    private enum class ValidationIconType {
        NONE, VALIDATION, PASSWORD_VALIDATION; // The order should match the order in the attr validationIconType
    }

    private enum class OscaInputValidationState { NONE, OK, ERROR; }

    private var validationIconType: ValidationIconType =
        ValidationIconType.NONE
    private val iconValidPlaceholder: Drawable? by lazy { iconValidOk?.constantState?.newDrawable()?.mutate() }
    private var _adaptValidationIcon: ((OscaInputValidationState) -> Unit)? = null
    private val iconValidOk: Drawable? by lazy { context.getDrawable(R.drawable.ic_icon_val_ok) }
    private val iconValidError: Drawable? by lazy { context.getDrawable(R.drawable.ic_icon_val_error) }

    private val inputLayout: TextInputLayout by lazy { findViewById(R.id.autofillInputLayout) }
    val editText: AutoCompleteTextView by lazy { findViewById(R.id.autofillEditText) }

    private var _onTextChanged: ((String) -> Unit)? = null
    private var _onFocusChanged: ((Boolean) -> Unit)? = null

    private var isUserTyping = true
    private var skipChange = false
    private var removeErrorOnChange = true

    var text: String = ""
        get() = editText.text.toString().trim()
        set(value) {
            isUserTyping = false
            field = value
            editText.setText(value)
            isUserTyping = true
        }

    var error: String? = null
        set(value) {
            field = value
            if (value.isNullOrEmpty()) {
                _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
            } else {
                inputLayout.setErrorTextColor(ColorStateList.valueOf(ERROR_COLOR))
                _adaptValidationIcon?.invoke(OscaInputValidationState.ERROR)
            }
            inputLayout.error = value
        }
    var ok: Boolean = false
        set(value) {
            field = value
            if (error != null) error = null
            if (value) {
                _adaptValidationIcon?.invoke(OscaInputValidationState.OK)
            } else {
                _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
            }
        }
    var validation: FieldValidation = FieldValidation(FieldValidation.IDLE, null)
        set(value) {
            if (field == value) return
            field = value

            when (value.state) {
                FieldValidation.IDLE -> {
                    _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
                    inputLayout.setErrorTextColor(ColorStateList.valueOf(Color.BLACK))
                }
                FieldValidation.OK, FieldValidation.SUCCESS -> {
                    _adaptValidationIcon?.invoke(OscaInputValidationState.OK)
                    inputLayout.setErrorTextColor(
                        ColorStateList.valueOf(OK_COLOR)
                    )
                }
                FieldValidation.ERROR, FieldValidation.NOT_OK -> {
                    _adaptValidationIcon?.invoke(OscaInputValidationState.ERROR)
                    inputLayout.setErrorTextColor(
                        ColorStateList.valueOf(ERROR_COLOR)
                    )
                }
            }

            inputLayout.error = value.message ?: if (value.stringRes != -1) context.getString(value.stringRes) else null
        }
    val hasErrors: Boolean get() = validation.state == FieldValidation.ERROR || validation.state == FieldValidation.NOT_OK

    init {
        inflate(context, R.layout.custom_autofill_input_view, this)

        editText.id = View.generateViewId()
        inputLayout.id = View.generateViewId()

        setViewParams(attrs)
        initViewListeners()
    }

    private fun setViewParams(attrs: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.OscaInputLayout)

        setupValidationIcon(typeArray)

        editText.apply {
            inputType = typeArray.getInt(R.styleable.OscaInputLayout_inputType, 1)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

            setText(typeArray.getString(R.styleable.OscaInputLayout_text))
        }
        removeErrorOnChange = typeArray.getBoolean(R.styleable.OscaInputLayout_removeErrorOnChange, true)

        inputLayout.apply {
            endIconMode =
                TextInputLayout.END_ICON_NONE

            setErrorTextAppearance(typeArray.getResourceId(R.styleable.OscaInputLayout_validationTextAppearance, 0))
            hint = typeArray.getString(R.styleable.OscaInputLayout_hint)

            setHintTextAppearance(typeArray.getResourceId(R.styleable.OscaInputLayout_hintTextAppearance, 0))
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isHintEnabled = true
            isHintAnimationEnabled = true
        }

        val isClickable = typeArray.getBoolean(R.styleable.OscaInputLayout_isClickable, false)

        if (isClickable) {
            editText.isFocusable = false
            editText.isClickable = true
            editText.setOnClickListener { this.performClick() }
        }

        typeArray.recycle()
    }

    private fun setupValidationIcon(typeArray: TypedArray) {
        val defaultIndex = OscaInputValidationState.NONE.ordinal
        val validationTypeIndex = typeArray.getInt(R.styleable.OscaInputLayout_validationIconType, defaultIndex)
        validationIconType = ValidationIconType.values()[validationTypeIndex]

        val adaptErrorIconForNonPassword = { validationState: OscaInputValidationState ->
            val validationIcon = when (validationState) {
                OscaInputValidationState.NONE -> iconValidPlaceholder
                OscaInputValidationState.OK -> iconValidOk
                OscaInputValidationState.ERROR -> iconValidError
            }
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, validationIcon, null)

            // instead of removing the icon completely, just set it transparent, so the padding won't be removed
            editText.post {
                editText.compoundDrawables[2]?.alpha = if (validationState == OscaInputValidationState.NONE) 0 else 255
            }
        }

        _adaptValidationIcon = when (validationIconType) {
            ValidationIconType.NONE -> null
            else -> { validationState -> adaptErrorIconForNonPassword(validationState) }
        }
    }

    private fun initViewListeners() {
        editText.textWatcher {
            onTextChanged { chars, _, before, count ->
                if (before != count) {
                    if (!skipChange) _onTextChanged?.invoke(chars.toString())
                    if (removeErrorOnChange) validation = FieldValidation(FieldValidation.IDLE, null)
                    if (!editText.isFocused && isUserTyping) editText.requestFocus()
                }
            }
        }

        editText.setOnFocusChangeListener { _, hasFocus ->
            _onFocusChanged?.invoke(hasFocus)
        }
    }

    fun onTextChanged(listener: ((String) -> Unit)) {
        _onTextChanged = listener
    }

    fun onFocusChanged(listener: (Boolean) -> Unit) {
        _onFocusChanged = listener
    }

    fun deactivateChildren() {
        editText.isEnabled = false
    }

    private fun activateChildren() {
        editText.isEnabled = true
    }

    fun requestFocusAtEnd() {
        editText.requestFocus()
        editText.setSelection(editText.text?.length ?: 0)
    }

    fun deactivate() {
        deactivateChildren()
        disable()
    }

    fun activate() {
        activateChildren()
        enable()
    }

    fun clear() {
        skipChange = true
        if (!editText.text.isNullOrBlank()) editText.setText("")
        inputLayout.error = null
        _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
        skipChange = false
    }

    override fun onSaveInstanceState(): Parcelable {
        return OscaInputLayoutState(text, validation, super.onSaveInstanceState(), validation.state)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        state as OscaInputLayoutState
        super.onRestoreInstanceState(state.viewState)
        text = state.text
        validation = state.validation
    }
}
