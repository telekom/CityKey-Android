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
 
package com.telekom.citykey.view.user.profile.change_email

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes.EMAIL_ALREADY_USED
import com.telekom.citykey.common.ErrorCodes.EMAIL_EMPTY
import com.telekom.citykey.common.ErrorCodes.EMAIL_EQUALS
import com.telekom.citykey.common.ErrorCodes.EMAIL_INVALID
import com.telekom.citykey.common.ErrorCodes.EMAIL_NO_EXIST
import com.telekom.citykey.common.ErrorCodes.MULTIPLE_ERRORS
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.EmailChangeRequest
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.Validation
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ChangeEmailViewModel(
    private val globalData: GlobalData,
    private val userRepository: UserRepository
) : NetworkingViewModel() {

    companion object {
        private const val CHANGE_EMAIL_API_TAG = "changeEmail"
    }

    val requestSent: LiveData<Boolean> get() = _requestSent
    val generalErrors: LiveData<Int> get() = _generalErrors
    val onlineErrors: LiveData<Pair<String?, String?>> get() = _onlineErrors
    val currentEmail: LiveData<String> get() = _currentEmail
    val logUserOut: LiveData<Unit> get() = _logUserOut
    val inputValidation: SingleLiveEvent<Int?> get() = _inputValidation

    private val _inputValidation: SingleLiveEvent<Int?> = SingleLiveEvent()
    private val _requestSent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _generalErrors: SingleLiveEvent<Int> = SingleLiveEvent()
    private val _onlineErrors: SingleLiveEvent<Pair<String?, String?>> = SingleLiveEvent()
    private val _currentEmail: MutableLiveData<String> = MutableLiveData()
    private val _logUserOut: SingleLiveEvent<Unit> = SingleLiveEvent()

    private var newEmail = ""

    init {
        launch {
            globalData.user
                .subscribeOn(Schedulers.io())
                .filter { it is UserState.Present }
                .map { (it as UserState.Present).profile.email }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_currentEmail::postValue)
        }
    }

    fun onSaveClicked(newEmail: String) {
        this.newEmail = newEmail

        launch {
            userRepository.changeEmail(EmailChangeRequest(newEmail.lowercase()))
                .retryOnError(this::onError, retryDispatcher, pendingRetries, CHANGE_EMAIL_API_TAG)
                .subscribe(
                    {
                        _requestSent.postValue(true)
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
                _showRetryDialog.postValue(CHANGE_EMAIL_API_TAG)
            }
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        MULTIPLE_ERRORS -> _generalErrors.postValue(R.string.p_003_profile_email_change_technical_error)
                        EMAIL_ALREADY_USED, EMAIL_INVALID, EMAIL_EMPTY, EMAIL_EQUALS, EMAIL_NO_EXIST -> _onlineErrors.postValue(it.userMsg to null)
                        else -> _technicalError.value = Unit
                    }
                }
            }
            else -> {
                _technicalError.value = Unit
            }
        }
    }

    fun onEmailChange(email: String) {
        if (!Validation.isEmailFormat(email)) {
            _inputValidation.value = R.string.r_001_registration_error_incorrect_email
        } else {
            _inputValidation.value = null
        }
    }
}
