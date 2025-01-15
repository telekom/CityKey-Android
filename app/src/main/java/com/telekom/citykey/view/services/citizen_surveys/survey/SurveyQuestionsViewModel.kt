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

package com.telekom.citykey.view.services.citizen_surveys.survey

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.surveys.SurveysInteractor
import com.telekom.citykey.domain.services.surveys.SurveysState
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.api.requests.TopicAnswers
import com.telekom.citykey.models.citizen_survey.Question
import com.telekom.citykey.models.citizen_survey.SubmitResponse
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class SurveyQuestionsViewModel(
    private val surveyId: Int,
    private val surveysInteractor: SurveysInteractor,
    private val globalData: GlobalData,
    private val adjustManager: AdjustManager,
    private val preferencesHelper: PreferencesHelper
) : NetworkingViewModel() {

    val questionData: LiveData<List<Question>> get() = _questionData
    val state: LiveData<SurveysState> get() = _submitState
    val color: LiveData<Int> get() = MutableLiveData(globalData.cityColor)
    val surveyDataSubmitted: LiveData<SubmitResponse> get() = _surveyDataSubmitted

    private val _submitState: MutableLiveData<SurveysState> = MutableLiveData()
    private val _questionData: MutableLiveData<List<Question>> = MutableLiveData()
    private val _surveyDataSubmitted: MutableLiveData<SubmitResponse> = MutableLiveData()
    val cityName: LiveData<String> get() = MutableLiveData(globalData.cityName)

    init {
        launch {
            surveysInteractor.getQuestionsForSurvey(surveyId)
                .retryOnError(::onInitError, retryDispatcher, pendingRetries)
                .subscribe(_questionData::postValue, ::onInitError)
        }
    }

    fun onNextClick(
        surveyTopicResponse: Map<String, MutableList<TopicAnswers>>,
        attemptedQuestion: Int,
        submitSurvey: Boolean = false
    ) {
        launch {
            surveysInteractor.setSurveyAnswer(
                surveyId,
                questionData.value?.size!!,
                attemptedQuestion,
                surveyTopicResponse,
                submitSurvey
            )
                .retryOnError(::onSubmitError, retryDispatcher, pendingRetries)
                .subscribe(_surveyDataSubmitted::setValue, this::onSubmitError)
        }
        if (submitSurvey) {
            adjustManager.trackEvent(R.string.submit_poll)
        }
    }

    private fun onInitError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> super._showRetryDialog.call()
            else -> _technicalError.call()
        }
    }

    private fun onSubmitError(throwable: Throwable) {
        when (throwable) {
            is InvalidRefreshTokenException -> {
                globalData.logOutUser(throwable.reason)
                _submitState.postValue(SurveysState.ServiceNotAvailable)
            }

            is NoConnectionException -> super._showRetryDialog.call()
            else -> {
                _submitState.postValue(SurveysState.Error)
            }
        }
    }

    fun isPreview(): Boolean {
        return preferencesHelper.isPreviewMode
    }
}
