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

package com.telekom.citykey.networkinterface.models.citizen_survey

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

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
