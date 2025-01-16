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

package com.telekom.citykey.view.user.profile.feedback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.api.requests.FeedbackRequest
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.Validation
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.user.registration.RegFields
import io.reactivex.android.schedulers.AndroidSchedulers

class FeedbackViewModel(
    private val oscaRepository: OscaRepository,
    private val cityInteractor: CityInteractor,
    private val userInteractor: UserInteractor,
    private val adjustManager: AdjustManager
) : NetworkingViewModel() {

    val feedbackSubmitted: LiveData<Unit> get() = _feedbackSubmitted
    val inputValidation: LiveData<Pair<String, FieldValidation>> get() = _inputValidation
    private val _feedbackSubmitted: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _inputValidation: MutableLiveData<Pair<String, FieldValidation>> = MutableLiveData()
    val profileContent: LiveData<String> get() = _profileContent
    private val _profileContent: MutableLiveData<String> = MutableLiveData()

    companion object {
        private const val FEEDBACK_API_TAG = "FEEDBACK"
    }

    init {
        observeProfile()
    }

    private fun observeProfile() =
        launch {
            userInteractor.user
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it is UserState.Present) {
                        _profileContent.value = it.profile.email
                    } else {
                        _profileContent.value = ""
                    }
                }
        }

    fun onSendClicked(feedBackBox: String, feedBackBox1: String, feedbackEmail: String) {
        launch {
            oscaRepository.sendFeedback(
                FeedbackRequest(feedBackBox, feedBackBox1, cityInteractor.cityName, email = feedbackEmail)
            )
                .retryOnError(
                    this::onError, retryDispatcher, pendingRetries, FEEDBACK_API_TAG
                )
                .subscribe({
                    adjustManager.trackEvent(R.string.send_feedback)
                    _feedbackSubmitted.postValue(Unit)
                }, this::onError)
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> _technicalError.postValue(Unit)
            is NoConnectionException -> _showRetryDialog.postValue(FEEDBACK_API_TAG)
            else -> _technicalError.postValue(Unit)
        }
    }

    fun onEmailReady(email: String) {
        if (email.isEmpty()) {
            _inputValidation.value = RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_empty_field
            )
        } else if (email.isNotEmpty() && !Validation.isEmailFormat(email)) {
            _inputValidation.value = RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_email
            )
        } else {
            _inputValidation.value = RegFields.EMAIL to FieldValidation(
                FieldValidation.SUCCESS,
                null,
                0
            )
        }
    }

}
