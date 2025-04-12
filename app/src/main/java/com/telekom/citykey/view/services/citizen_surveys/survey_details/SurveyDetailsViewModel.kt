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

package com.telekom.citykey.view.services.citizen_surveys.survey_details

import androidx.lifecycle.LiveData
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.surveys.SurveysInteractor
import com.telekom.citykey.networkinterface.models.content.DataPrivacyNoticeResponse
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
