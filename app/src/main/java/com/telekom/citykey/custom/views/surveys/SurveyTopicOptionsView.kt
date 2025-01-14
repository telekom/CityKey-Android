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

package com.telekom.citykey.custom.views.surveys

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyTopicOptionBinding
import com.telekom.citykey.models.api.requests.TopicAnswers
import com.telekom.citykey.models.citizen_survey.Topic
import com.telekom.citykey.utils.EmptyTextWatcher
import com.telekom.citykey.utils.extensions.isNotVisible
import com.telekom.citykey.utils.extensions.setAccessibilityBasedOnViewStateSelection
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.textWatcher

class SurveyTopicOptionsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val bindings = mutableListOf<SurveyTopicOptionBinding>()

    var selectionListener: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(hasSelectedOption)
        }

    private val marginInDp = context.resources.getDimension(R.dimen.global_content_margins).toInt()
    private var radioList = mutableListOf<MaterialCardView>()
    private var checkBoxList = mutableListOf<MaterialCardView>()
    private var openQnList = mutableListOf<MaterialCardView>()
    private var hasSelectedOption = false
    val textWatcher = object : EmptyTextWatcher() {
        override fun afterTextChanged(s: Editable?) {
            hasSelectedOption = true
            selectionListener?.invoke(hasSelectedOption)
            openQnList.forEachIndexed { index, _ ->
                val binding = bindings[index]
                if (binding.textInputLayoutForOQ == s) {
                    binding.textInputLayoutForOQ.error = ""
                    binding.textInputEditTextForOQ.text?.clear()
                    binding.textInputEditTextForOQ.hint = ""
                    binding.textInputEditTextForOQ.textWatcher {
                        onTextChanged { _, _, _, _ ->
                            binding.textInputEditTextForOQ.hint = ""
                            binding.textInputLayoutForOQ.error = ""
                        }
                    }
                    binding.textInputEditTextForOQ.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                                rootView.windowToken,
                                0
                            )
                        }
                    }
                }
            }
        }
    }
    private val radioChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            radioList.forEachIndexed { index, _ ->
                if (bindings[index].radioAns != buttonView) bindings[index].radioAns.isChecked = false
            }
            buttonView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
            hasSelectedOption = true
        } else {
            if (buttonView is CheckBox) {
                hasSelectedOption =
                    checkBoxList.filterIndexed { index, _ -> bindings[index].checkboxAns.isChecked }.isNotEmpty()
            }
            buttonView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        selectionListener?.invoke(hasSelectedOption)
        if (buttonView.tag == true) {
            radioList.forEachIndexed { index, _ ->
                val binding = bindings[index]
                if (binding.radioAns == buttonView) {
                    binding.textInputLayout.error = ""
                    binding.textInputEditText.text?.clear()
                    binding.textInputEditText.hint = ""
                    if (binding.textInputDescription.text.isNotBlank()) {
                        binding.textInputDescription.setVisible(isChecked)
                    }
                    binding.textInputLayout.setVisible(isChecked)
                    binding.textInputEditText.textWatcher {
                        onTextChanged { _, _, _, _ ->
                            binding.textInputEditText.hint = ""
                            binding.textInputLayout.error = ""
                        }
                    }

                    binding.textInputEditText.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            (
                                    context.getSystemService(
                                        Context.INPUT_METHOD_SERVICE
                                    ) as InputMethodManager
                                    ).hideSoftInputFromWindow(
                                    rootView.windowToken,
                                    0
                                )
                        }
                    }
                }
            }

            checkBoxList.forEachIndexed { index, _ ->
                val binding = bindings[index]
                if (binding.checkboxAns == buttonView) {
                    binding.textInputLayout.error = ""
                    binding.textInputEditText.text?.clear()
                    binding.textInputEditText.hint = ""
                    if (binding.textInputDescription.text.isNotBlank()) {
                        binding.textInputDescription.setVisible(isChecked)
                    }
                    binding.textInputLayout.setVisible(isChecked)
                    binding.textInputEditText.textWatcher {
                        onTextChanged { _, _, _, _ ->
                            binding.textInputEditText.hint = ""
                            binding.textInputLayout.error = ""
                        }
                    }
                    binding.textInputEditText.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                                rootView.windowToken,
                                0
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    fun setupOptions(topic: Topic, @ColorInt color: Int) {
        radioList.clear()
        checkBoxList.clear()
        openQnList.clear()
        bindings.clear()
        removeAllViews()

        val optionColorStateList = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(Color.DKGRAY, color)
        )

        topic.options.forEachIndexed { index, option ->
            val cardView: MaterialCardView =
                LayoutInflater.from(context).inflate(R.layout.survey_topic_option, null) as MaterialCardView

            val binding = SurveyTopicOptionBinding.bind(cardView)
            if (index != 0 && topic.topicDesignType == Topic.DESIGN_UNFRAMED) {
                val parms = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                parms.setMargins(0, marginInDp, 0, 0)
                cardView.layoutParams = parms
            }

            val strokeColor = when (topic.topicOptionType) {
                "OQ" -> ContextCompat.getColor(this.context, R.color.wasteCalendarWidgetGradientStart)
                else -> ContextCompat.getColor(this.context, R.color.onSurfaceSecondary)
            }
            binding.card.strokeColor = strokeColor
            when (topic.topicOptionType) {
                "CB" -> {
                    binding.checkboxAns.visibility = View.VISIBLE
                    binding.checkboxAns.text = option.optionText
                    binding.checkboxAns.setOnCheckedChangeListener(radioChangeListener)
                    option.textAreaMandatory.let {
                        when (it) {
                            is Boolean -> binding.root.tag = it
                            is Double -> binding.root.tag = it == 1.0
                            is Int -> binding.root.tag = it == 1
                        }
                    }
                    option.hasTextArea.let {
                        when (it) {
                            is Boolean -> binding.checkboxAns.tag = it
                            is Double -> binding.checkboxAns.tag = it == 1.0
                            is Int -> binding.checkboxAns.tag = it == 1
                        }
                    }
                    binding.checkboxAns.buttonTintList = optionColorStateList
                    checkBoxList.add(cardView)
                }

                "RB" -> {
                    binding.radioAns.visibility = View.VISIBLE
                    binding.radioAns.text = option.optionText
                    binding.radioAns.setOnCheckedChangeListener(radioChangeListener)
                    option.textAreaMandatory.let {
                        when (it) {
                            is Boolean -> binding.root.tag = it
                            is Double -> binding.root.tag = it == 1.0
                            is Int -> binding.root.tag = it == 1
                        }
                    }
                    option.hasTextArea.let {
                        when (it) {
                            is Boolean -> binding.radioAns.tag = it
                            is Double -> binding.radioAns.tag = it == 1.0
                            is Int -> binding.radioAns.tag = it == 1
                        }
                    }
                    binding.radioAns.buttonTintList = optionColorStateList
                    radioList.add(cardView)
                }

                "OQ" -> {
                    option.textAreaMandatory.let {
                        when (it) {
                            is Boolean -> binding.root.tag = it
                            is Double -> binding.root.tag = it == 1.0
                            is Int -> binding.root.tag = it == 1
                        }
                    }
                    binding.radioAns.visibility = View.GONE
                    binding.checkboxAns.visibility = View.GONE
                    binding.textInputLayout.visibility = View.GONE
                    binding.textInputDescription.visibility = View.GONE
                    binding.textInputEditTextForOQ.visibility = View.VISIBLE
                    binding.textInputLayoutForOQ.visibility = View.VISIBLE
                    binding.textInputEditTextForOQ.addTextChangedListener(textWatcher)
                    topic.options[0].textAreaMandatory.let {
                        when (it) {
                            is Int -> {
                                hasSelectedOption = it != 1
                            }

                            is Double -> {
                                hasSelectedOption = it != 1.0
                            }

                            is Boolean -> {
                                hasSelectedOption = !it
                            }
                        }
                    }
                    openQnList.add(cardView)
                }
            }
            option.hasTextArea.let {
                when (it) {
                    is Int -> if (it == 1 && topic.topicOptionType != "OQ") {
                        binding.textInputDescription.text = option.textAreaDescription
                    }

                    is Boolean -> if (it == true && topic.topicOptionType != "OQ") {
                        binding.textInputDescription.text = option.textAreaDescription
                    }

                    is Double -> if (it == 1.0 && topic.topicOptionType != "OQ") {
                        binding.textInputDescription.text = option.textAreaDescription
                    }
                }
            }
            if (topic.topicDesignType == Topic.DESIGN_UNFRAMED) cardView.strokeWidth = 2

            bindings.add(binding)
            addView(cardView)
        }
    }

    fun collectAnswers(topic: Topic, questionId: String): MutableList<TopicAnswers> {
        val topicAnswerList = mutableListOf<TopicAnswers>()

        bindings.find { it.radioAns.isChecked }?.let { binding ->
            if (binding.root.tag == true && binding.textInputEditText.text.isNullOrBlank()) {
                binding.textInputEditText.text?.clear()
                binding.textInputEditText.hint =
                    binding.root.context.getString(R.string.cs_004_mandatory_field_error)
                binding.textInputLayout.error =
                    binding.root.context.getString(R.string.cs_004_mandatory_field_error)
                requestFocus()
                binding.textInputLayout.getChildAt(1).visibility = GONE
                topicAnswerList.clear()
            } else {
                topicAnswerList.add(
                    TopicAnswers(
                        questionId.toInt(),
                        topic.topicId.toInt(),
                        topic.options.find { binding.radioAns.text == it.optionText }?.optionNo?.toInt(),
                        binding.textInputEditText.text.toString()
                    )
                )
            }
        }

        bindings.forEach { binding ->
            if (binding.checkboxAns.isChecked) {
                if (binding.root.tag == true && binding.textInputEditText.text.isNullOrBlank()) {
                    binding.textInputEditText.text?.clear()
                    binding.textInputEditText.hint =
                        binding.root.context.getString(R.string.cs_004_mandatory_field_error)
                    binding.textInputLayout.error =
                        binding.root.context.getString(R.string.cs_004_mandatory_field_error)
                    binding.textInputLayout.getChildAt(1).visibility = GONE
                    topicAnswerList.clear()
                } else {
                    topicAnswerList.add(
                        TopicAnswers(
                            questionId.toInt(),
                            topic.topicId.toInt(),
                            topic.options.find { binding.checkboxAns.text == it.optionText }?.optionNo?.toInt(),
                            binding.textInputEditText.text.toString()
                        )
                    )
                }
            }
            binding.checkboxAns.setAccessibilityBasedOnViewStateSelection(binding.checkboxAns.isChecked)
            binding.checkboxAns.setOnClickListener { setAccessibilityBasedOnViewStateSelection(it.isSelected) }
        }

        //confirm which option number we need to send in case of OQ
        bindings.forEach { binding ->
            if (binding.checkboxAns.isNotVisible and binding.radioAns.isNotVisible) {
                if (binding.root.tag == true && binding.textInputEditTextForOQ.text.isNullOrBlank()) {
                    binding.textInputEditTextForOQ.text?.clear()
                    binding.textInputEditTextForOQ.hint =
                        binding.root.context.getString(R.string.cs_004_mandatory_field_error)
                    binding.textInputLayoutForOQ.error =
                        binding.root.context.getString(R.string.cs_004_mandatory_field_error)
                    binding.textInputLayoutForOQ.getChildAt(1).visibility = GONE
                    topicAnswerList.clear()
                } else {
                    topicAnswerList.add(
                        TopicAnswers(
                            questionId.toInt(),
                            topic.topicId.toInt(), 1,
                            binding.textInputEditTextForOQ.text.toString()
                        )
                    )
                }
            }
        }
        return topicAnswerList
    }
}
