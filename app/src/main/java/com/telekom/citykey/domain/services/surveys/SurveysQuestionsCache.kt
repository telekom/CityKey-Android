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

package com.telekom.citykey.domain.services.surveys

import android.annotation.SuppressLint
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.api.requests.SubmitSurveyRequest
import com.telekom.citykey.networkinterface.models.api.requests.TopicAnswers
import com.telekom.citykey.networkinterface.models.citizen_survey.Question
import com.telekom.citykey.networkinterface.models.citizen_survey.SubmitResponse
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class SurveysQuestionsCache(
    private val globalData: GlobalData,
    private val servicesRepository: ServicesRepository
) {

    private val submitList = mutableListOf<TopicAnswers>()
    private val mapAnswers = mutableMapOf<String, MutableList<TopicAnswers>>()
    private val _questions = mutableMapOf<Int, List<Question>>()
    private var attemptedQuestions = 0

    init {
        observeCityAndUser()
    }

    @SuppressLint("CheckResult")
    private fun observeCityAndUser() {
        Observable.combineLatest(
            globalData.user.filter { it is UserState.Absent },
            globalData.city.distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
        ) { _, _ -> }
            .subscribe { _questions.clear() }
    }

    fun setSurveyAnswer(
        surveyId: Int,
        totalQuestions: Int,
        attemptedQuestions: Int,
        topicResponse: Map<String, MutableList<TopicAnswers>>,
        submitSurvey: Boolean
    ): Maybe<SubmitResponse> {
        topicResponse.forEach { mapAnswers[it.key] = it.value }

        this.attemptedQuestions = attemptedQuestions
        if (submitSurvey) {
            submitList.clear()
            val submitSurveyRequest = SubmitSurveyRequest(totalQuestions, attemptedQuestions, submitList)
            mapAnswers.forEach { submitList.addAll(it.value) }
            return servicesRepository.submitSurvey(globalData.currentCityId, surveyId, submitSurveyRequest)
        }
        return Maybe.just(SubmitResponse(false))
    }

    fun getSurveyQuestions(surveyId: Int): Maybe<List<Question>> {
        _questions[surveyId]?.let { return Maybe.just(it) }

        return servicesRepository.getSurveyQuestions(surveyId, globalData.currentCityId)
            .doOnSuccess { _questions[surveyId] = it }
            .observeOn(AndroidSchedulers.mainThread())
    }
}
