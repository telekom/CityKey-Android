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

package com.telekom.citykey.view.user.profile.change_residence

import androidx.lifecycle.LiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.data.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.networkinterface.models.api.requests.PersonalDetailChangeRequest
import com.telekom.citykey.networkinterface.models.user.ResidenceValidationResponse
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class ChangeResidenceViewModel(
    private val globalData: GlobalData,
    private val userRepository: UserRepository,
    private val userInteractor: UserInteractor
) : NetworkingViewModel() {

    companion object {
        private const val CHANGE_POSTAL_CODE_API_TAG = "Change_Postcode"
    }

    val saveSuccessful: LiveData<Boolean> get() = _saveSuccessful
    val requestSent: LiveData<ResidenceValidationResponse> get() = _requestSent
    val generalErrors: LiveData<Int> get() = _generalErrors
    val onlineErrors: LiveData<Pair<String?, String?>> get() = _onlineErrors
    val logUserOut: LiveData<Unit> get() = _logUserOut
    val inputValidation: SingleLiveEvent<Int> get() = _inputValidation

    private val _saveSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _inputValidation: SingleLiveEvent<Int> = SingleLiveEvent()
    private val _requestSent: SingleLiveEvent<ResidenceValidationResponse> = SingleLiveEvent()
    private val _generalErrors: SingleLiveEvent<Int> = SingleLiveEvent()
    private val _onlineErrors: SingleLiveEvent<Pair<String?, String?>> = SingleLiveEvent()
    private val _logUserOut: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onSaveClicked(postalCode: String) {
        if (postalCode.length == 5) {
            launch {
                userRepository.validatePostalCode(postalCode)
                    .retryOnError(this::onError, retryDispatcher, pendingRetries)
                    .subscribe(
                        {
                            _requestSent.value = it
                            savePostalCode(postalCode, it.cityName, it.homeCityId)
                        },
                        this::onError
                    )
            }
        } else {
            _inputValidation.value = R.string.r_001_registration_error_incorrect_postcode
        }
    }

    private fun savePostalCode(postalCode: String, newCityName: String, newCityId: Int) {
        launch {
            userRepository.changePersonalData(PersonalDetailChangeRequest("", postalCode), "postalCode")
                .retryOnError(this::onError, retryDispatcher, pendingRetries, CHANGE_POSTAL_CODE_API_TAG)
                .subscribe(
                    {
                        userInteractor.updatePersonalDataLocally(postalCode, newCityName, newCityId)
                        _saveSuccessful.postValue(true)
                    },
                    this::onError
                )
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is InvalidRefreshTokenException -> {
                globalData.logOutUser(throwable.reason)
                _logUserOut.postValue(Unit)
            }

            is NoConnectionException -> {
                _showRetryDialog.postValue(CHANGE_POSTAL_CODE_API_TAG)
            }

            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.MULTIPLE_ERRORS -> _generalErrors.postValue(R.string.p_003_profile_email_change_technical_error)
                        ErrorCodes.CHANGE_POSTAL_CODE_INVALID, ErrorCodes.CHANGE_POSTAL_CODE_VALIDATION_ERROR -> _onlineErrors.postValue(
                            it.userMsg to null
                        )

                        else -> _technicalError.value = Unit
                    }
                }
            }

            else -> {
                _technicalError.value = Unit
            }
        }
    }
}
