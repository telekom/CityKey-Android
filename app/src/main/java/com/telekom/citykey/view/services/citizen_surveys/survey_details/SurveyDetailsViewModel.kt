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

package com.telekom.citykey.view.services.citizen_surveys.survey_details

import androidx.lifecycle.LiveData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.surveys.SurveysInteractor
import com.telekom.citykey.models.content.DataPrivacyNoticeResponse
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class SurveyDetailsViewModel(
    private val surveysInteractor: SurveysInteractor, private val preferencesHelper: PreferencesHelper
) : NetworkingViewModel() {

    val surveyAvailable: LiveData<Unit> get() = _surveyAvailable
    val surveyDataPrivacyAccepted: LiveData<Boolean> get() = _surveyDataPrivacyAccepted
    val surveyDataPrivacy: LiveData<DataPrivacyNoticeResponse> get() = _surveyDataPrivacy

    private val _surveyAvailable: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _surveyDataPrivacyAccepted: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _surveyDataPrivacy: SingleLiveEvent<DataPrivacyNoticeResponse> = SingleLiveEvent()

    fun onStartSurveyClicked(surveyId: Int) {
        if (surveysInteractor.dataPrivacyMap[surveyId] == true || isPreview()) {
            launch {
                surveysInteractor.getQuestionsForSurvey(surveyId)
                    .retryOnError(this::onRequestError, retryDispatcher, pendingRetries)
                    .subscribe(
                        { _surveyAvailable.postValue(Unit) },
                        this::onRequestError
                    )
            }
        } else {
            launch {
                surveysInteractor.getDataPrivacyNoticeForSurvey()
                    .retryOnError(this::onRequestError, retryDispatcher, pendingRetries)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            _surveyDataPrivacyAccepted.postValue(false)
                            _surveyDataPrivacy.postValue(it)

                        },
                        this::onRequestError
                    )
            }
        }
    }

    fun onDataPrivacyAccepted(surveyId: Int) {
        surveysInteractor.setSurveyPrivacyNoticeAccepted(surveyId)
    }

    private fun onRequestError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> showRetry()
            else -> _technicalError.postValue(Unit)
        }
    }

    private fun isPreview(): Boolean {
        return preferencesHelper.isPreviewMode
    }
}
