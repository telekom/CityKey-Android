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
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.textWatcher

@SuppressLint("UseCompatLoadingForDrawables")
class OscaInputLayout(context: Context, attrs: AttributeSet) :
    TextInputLayout(context, attrs, R.style.Widget_Design_TextInputLayout) {

    companion object {
        private const val DEFAULT_MAX_LENGTH = 255
        private const val ERROR_COLOR = 0xFF_FF_5F_5F.toInt()
        private const val OK_COLOR = 0xFF_23_9D_1D.toInt()
    }

    private enum class ValidationIconType {
        NONE, VALIDATION, PASSWORD_VALIDATION; // The order should match the order in the attr validationIconType
    }

    private enum class OscaInputValidationState { NONE, OK, ERROR; }

    private var validationIconType: ValidationIconType =
        ValidationIconType.NONE
    private var _adaptValidationIcon: ((OscaInputValidationState) -> Unit)? = null
    private val iconValidPlaceholder: Drawable? by lazy { iconValidOk?.constantState?.newDrawable()?.mutate() }
    private val iconValidOk: Drawable? by lazy { context.getDrawable(R.drawable.ic_icon_val_ok) }
    private val iconValidError: Drawable? by lazy { context.getDrawable(R.drawable.ic_icon_val_error) }
    private val iconPasswordToggle: Drawable? by lazy { context.getDrawable(R.drawable.selector_password_toggle) }
    private val iconPasswordValidOk: Drawable? by lazy { context.getDrawable(R.drawable.layer_input_password_val_ok) }
    private val iconPasswordValidError: Drawable? by lazy { context.getDrawable(R.drawable.layer_input_password_val_error) }

    @get:JvmName("_editText")
    val editText: EditText
        get() = super.getEditText()!!

    private var removeErrorOnChange = true
    private var _onTextChanged: ((String) -> Unit)? = null
    private var _onFocusChanged: ((Boolean) -> Unit)? = null
    private var maxLength = 0
    private var isUserTyping = true

    private var skipChange = false

    var text: String = ""
        get() = editText.text.toString().trim()
        set(value) {
            isUserTyping = false
            field = value
            editText.setText(value)
            isUserTyping = true
        }
    var selection: Int = 0
        set(value) {
            field = value
            if (value > maxLength) {
                editText.setSelection(maxLength)
            } else {
                editText.setSelection(value)
            }
        }

    var error: String? = null
        set(value) {
            field = value
            if (value.isNullOrEmpty()) {
                _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
            } else {
                setErrorTextColor(ColorStateList.valueOf(ERROR_COLOR))
                _adaptValidationIcon?.invoke(OscaInputValidationState.ERROR)
            }
            super.setError(value)
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
                FieldValidation.HELPER -> {
                    _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
                    if (value.stringRes != -1) {
                        helperText = context.getString(value.stringRes)
                    }
                    return
                }

                FieldValidation.IDLE -> {
                    _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
                    setErrorTextColor(ColorStateList.valueOf(Color.BLACK))
                }

                FieldValidation.OK, FieldValidation.SUCCESS -> {
                    _adaptValidationIcon?.invoke(OscaInputValidationState.OK)
                    setErrorTextColor(
                        ColorStateList.valueOf(OK_COLOR)
                    )
                }

                FieldValidation.ERROR, FieldValidation.NOT_OK -> {
                    _adaptValidationIcon?.invoke(OscaInputValidationState.ERROR)
                    setErrorTextColor(
                        ColorStateList.valueOf(ERROR_COLOR)
                    )
                }
            }

            val error = value.message ?: if (value.stringRes != -1) context.getString(value.stringRes) else null
            super.setError(error)
        }

    val hasErrors: Boolean get() = validation.state == FieldValidation.ERROR || validation.state == FieldValidation.NOT_OK

    init {
        addView(TextInputEditText(context))
        isSaveEnabled = true

        setViewParams(attrs)
        initViewListeners()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
    }

    private fun setViewParams(attrs: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.OscaInputLayout)

        setupValidationIcon(typeArray)

        editText.apply {
            inputType = typeArray.getInt(R.styleable.OscaInputLayout_inputType, 1)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            maxLength = typeArray.getInt(
                R.styleable.OscaInputLayout_maxLength,
                DEFAULT_MAX_LENGTH
            )
            val lengthFilter = InputFilter.LengthFilter(maxLength)

            val prohibitSpecialChars = typeArray.getBoolean(R.styleable.OscaInputLayout_prohibitSpecialChars, false)
            val prohibitEmptySpace = typeArray.getBoolean(R.styleable.OscaInputLayout_prohibitEmptySpace, false)

            filters = arrayOf(lengthFilter)
            if (prohibitSpecialChars) prohibitSpecialChars()
            if (prohibitEmptySpace) prohibitEmptySpace()

            setText(typeArray.getString(R.styleable.OscaInputLayout_text))
        }

        removeErrorOnChange = typeArray.getBoolean(R.styleable.OscaInputLayout_removeErrorOnChange, true)

        endIconMode =
            if (validationIconType == ValidationIconType.PASSWORD_VALIDATION)
                END_ICON_PASSWORD_TOGGLE else END_ICON_NONE
        setErrorTextAppearance(typeArray.getResourceId(R.styleable.OscaInputLayout_validationTextAppearance, 0))
        hint = typeArray.getString(R.styleable.OscaInputLayout_hint)

        setHintTextAppearance(typeArray.getResourceId(R.styleable.OscaInputLayout_hintTextAppearance, 0))
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isHintEnabled = true
        isHintAnimationEnabled = true

        val isClickable = typeArray.getBoolean(R.styleable.OscaInputLayout_isClickable, false)

        if (isClickable) {
            editText.isFocusable = false
            editText.isClickable = true
            editText.setOnClickListener { this.performClick() }
        }

        typeArray.recycle()
    }

    private fun setupValidationIcon(typeArray: TypedArray) {
        val defaultIndex = ValidationIconType.NONE.ordinal
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
        val adaptErrorIconForPassword = { validationState: OscaInputValidationState ->
            val validationIcon = when (validationState) {
                OscaInputValidationState.NONE -> iconPasswordToggle
                OscaInputValidationState.OK -> iconPasswordValidOk
                OscaInputValidationState.ERROR -> iconPasswordValidError
            }
            endIconDrawable = validationIcon
        }

        _adaptValidationIcon = when (validationIconType) {
            ValidationIconType.NONE -> null
            ValidationIconType.VALIDATION -> { validationState -> adaptErrorIconForNonPassword(validationState) }
            ValidationIconType.PASSWORD_VALIDATION -> { validationState -> adaptErrorIconForPassword(validationState) }
        }
    }

    private fun initViewListeners() {
        editText.textWatcher {
            onTextChanged { chars, _, before, count ->
                if (before != count) {
                    if (removeErrorOnChange) validation = FieldValidation(FieldValidation.IDLE, null)
                    if (!skipChange) _onTextChanged?.invoke(chars.toString())
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
        validation = FieldValidation(FieldValidation.IDLE, null)
        _adaptValidationIcon?.invoke(OscaInputValidationState.NONE)
        skipChange = false
    }

    private fun prohibitSpecialChars() {
        addInputFilter { s, _, _, _, _, _ ->
            if (s.contains(Regex("[<>&*'\"]")))
                s.replace(Regex("[<>&*'\"]"), "")
            else
                s
        }
    }

    private fun prohibitEmptySpace() {
        addInputFilter { s, _, _, _, _, _ ->
            if (s.contains(Regex("[ ]")))
                s.replace(Regex("[ ]"), "")
            else
                s
        }
    }

    private fun addInputFilter(newInputFilter: InputFilter) {
        editText.apply {
            val currentFilters = filters.toCollection(ArrayList())
            currentFilters.add(newInputFilter)

            val newFilters = arrayOfNulls<InputFilter>(currentFilters.size)
            filters = currentFilters.toArray(newFilters)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return OscaInputLayoutState(text, validation, super.onSaveInstanceState(), validation.state)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state as OscaInputLayoutState
        super.onRestoreInstanceState(state.viewState)
        text = state.text
        validation = state.validation
    }
}
