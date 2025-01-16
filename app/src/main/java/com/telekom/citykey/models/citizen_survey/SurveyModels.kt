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

package com.telekom.citykey.models.citizen_survey

import android.os.Parcelable
import com.telekom.citykey.utils.extensions.toCalendar
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.concurrent.TimeUnit

@Parcelize
data class Survey(
    val id: Int,
    val name: String,
    val description: String,
    val startDate: Date,
    val endDate: Date,
    val isClosed: Boolean,
    val isPopular: Boolean,
    var status: String,
    val dataProtectionText: String,
    val totalQuestions: Int,
    val attemptedQuestion: Int,
    val isDpAccepted: Boolean,
    val daysLeft: Int
) : Parcelable {
    companion object {
        const val STATUS_STARTED = "STARTED"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_NOT_STARTED = "NOT_STARTED"
    }

    val daysTotal: Int
        get() = TimeUnit.MILLISECONDS.toDays(endDate.toCalendar().timeInMillis - startDate.toCalendar().timeInMillis)
            .toInt()
}

data class SurveyQuestions(
    val totalQuestions: Int,
    val questions: List<Question>
)

data class Question(
    val questionId: String,
    val questionAnswered: String,
    val questionOrder: String,
    val questionText: String,
    val questionHint: String,
    val topics: List<Topic>
)

data class Topic(
    val topicId: String,
    val topicName: String,
    val topicDesignType: Int, // 1: Unframed topic, 2: Framed topic
    val topicOrder: String,
    val topicOptionType: String,
    val options: List<Option>
) {
    companion object {
        const val DESIGN_FRAMED = 2
        const val DESIGN_UNFRAMED = 1
    }
}

data class Option(
    val optionNo: String,
    val optionText: String,
    val optionSelected: Any,
    val hasTextArea: Any,
    val textAreaMandatory: Any,
    val textAreaDescription: String,
    val textAreaInput: String
)

data class SubmitResponse(val isSuccessful: Boolean)
