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

package com.telekom.citykey.domain.services.surveys

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.models.api.requests.TopicAnswers
import com.telekom.citykey.models.citizen_survey.Question
import com.telekom.citykey.models.citizen_survey.SubmitResponse
import com.telekom.citykey.models.citizen_survey.Survey
import com.telekom.citykey.models.content.DataPrivacyNoticeResponse
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class SurveysInteractor(
    private val servicesRepository: ServicesRepository,
    private val globalData: GlobalData,
    private val servicesInteractor: ServicesInteractor,
    private val surveysQuestionsCache: SurveysQuestionsCache
) {
    val surveys: Observable<List<Survey>> get() = _surveysSubject.hide()
    val state: LiveData<SurveysState> get() = _surveysState

    private val _surveysSubject: BehaviorSubject<List<Survey>> = BehaviorSubject.create()
    private val _surveysState: MutableLiveData<SurveysState> = MutableLiveData()
    var dataPrivacyMap = mutableMapOf<Int, Boolean>()

    init {
        observeServices()
    }

    @SuppressLint("CheckResult")
    private fun observeServices() {
        servicesInteractor.state
            .filter { it !is ServicesStates.Loading }
            .map {
                it is ServicesStates.Success &&
                        it.data.services.find { service -> service.function == ServicesFunctions.SURVEYS } != null
            }
            .switchMap { isServiceAvailable ->
                return@switchMap if (isServiceAvailable) {
                    _surveysState.postValue(SurveysState.Loading)
                    servicesRepository.getSurveys(globalData.currentCityId)
                        .doOnSuccess {
                            _surveysState.postValue(
                                if (it.isEmpty()) SurveysState.Empty else SurveysState.Success
                            )
                        }
                        .onErrorReturn {
                            _surveysState.postValue(SurveysState.Error)
                            emptyList()
                        }
                        .toFlowable()
                } else {
                    _surveysState.postValue(SurveysState.ServiceNotAvailable)
                    Flowable.just(emptyList())
                }
            }
            .subscribe(_surveysSubject::onNext, Timber::e)
    }

    fun isSurveyPreviewAvailable(): Boolean {
        return _surveysState.value in listOf(SurveysState.Success, SurveysState.Empty, SurveysState.Loading)
    }

    fun getQuestionsForSurvey(surveyId: Int): Maybe<List<Question>> = surveysQuestionsCache.getSurveyQuestions(surveyId)

    fun getDataPrivacyNoticeForSurvey(): Maybe<DataPrivacyNoticeResponse> =
        servicesRepository.getDataPrivacyNoticeForSurvey(globalData.currentCityId)

    fun setSurveyPrivacyNoticeAccepted(id: Int) {
        dataPrivacyMap[id] = true
    }

    private fun setSurveyCompleted(surveyId: Int) {
        _surveysSubject.value?.apply { find { it.id == surveyId }?.status = Survey.STATUS_COMPLETED }
            ?.also(_surveysSubject::onNext)
    }

    fun setSurveyAnswer(
        surveyId: Int,
        totalQuestions: Int,
        attemptedQuestions: Int,
        topicResponse: Map<String, MutableList<TopicAnswers>>,
        submitSurvey: Boolean
    ): Maybe<SubmitResponse> =
        surveysQuestionsCache.setSurveyAnswer(surveyId, totalQuestions, attemptedQuestions, topicResponse, submitSurvey)
            .doOnSuccess { if (it.isSuccessful) setSurveyCompleted(surveyId) }

    fun refreshSurveys(): Completable {
        return if (globalData.isUserLoggedIn) {
            _surveysState.postValue(SurveysState.Loading)
            servicesRepository.getSurveys(globalData.currentCityId)
                .doOnError {
                    when (it) {
                        is InvalidRefreshTokenException -> {
                            globalData.logOutUser(it.reason)
                            _surveysSubject.onNext(emptyList())
                            _surveysState.postValue(SurveysState.ServiceNotAvailable)
                        }

                        else -> {
                            if (_surveysState.value != SurveysState.Success)
                                _surveysState.postValue(SurveysState.Error)
                        }
                    }
                }
                .doOnSuccess {
                    _surveysSubject.onNext(it)
                    _surveysState.postValue(if (it.isEmpty()) SurveysState.Empty else SurveysState.Success)
                }
                .ignoreElement()
                .onErrorComplete()
        } else {
            _surveysState.postValue(SurveysState.ServiceNotAvailable)
            Completable.complete()
        }
    }
}
