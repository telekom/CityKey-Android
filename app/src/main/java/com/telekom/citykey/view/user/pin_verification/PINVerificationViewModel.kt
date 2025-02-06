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

package com.telekom.citykey.view.user.pin_verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.PinConfirmationRequest
import com.telekom.citykey.models.api.requests.ResendPinRequest
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.pin_verification.success.VerificationSuccess

class PINVerificationViewModel(private val userRepository: UserRepository, private val globalData: GlobalData) :
    NetworkingViewModel() {

    val emailResent: LiveData<Boolean> get() = _emailResent
    val resendError: LiveData<String> get() = _resendError
    val pinConfirmed: LiveData<Int> get() = _pinConfirmed
    val confirmError: LiveData<String> get() = _confirmError

    private val _emailResent: MutableLiveData<Boolean> = MutableLiveData()
    private val _resendError: SingleLiveEvent<String> = SingleLiveEvent()
    private val _confirmError: SingleLiveEvent<String> = SingleLiveEvent()
    private val _pinConfirmed: SingleLiveEvent<Int> = SingleLiveEvent()

    fun onResendEmailClicked(email: String, actionName: String) {
        launch {
            userRepository.resendPIN(ResendPinRequest(email.lowercase()), actionName = actionName)
                .retryOnError(this::onResendEmailError, retryDispatcher, pendingRetries, "callEmail")
                .subscribe({ _emailResent.postValue(true) }, ::onResendEmailError)
        }
    }

    fun onPinConfirmed(pin: String, email: String, actionName: String, actionType: Int) {
        launch {
            userRepository.confirmRegistration(PinConfirmationRequest(email.lowercase(), pin), actionName)
                .retryOnError(this::onConfirmPinError, retryDispatcher, pendingRetries, "confirmation")
                .subscribe(
                    {
                        _pinConfirmed.value = actionType
                        if (actionType == VerificationSuccess.EMAIL_CHANGED) globalData.logOutUser(LogoutReason.NO_LOGOUT_REASON)
                    },
                    ::onConfirmPinError
                )
        }
    }

    private fun onResendEmailError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                _resendError.value =
                    (throwable.error as OscaErrorResponse).errors.joinToString { it.userMsg }
            }
            is NoConnectionException -> _showRetryDialog.postValue(null)
            else -> _technicalError.postValue(Unit)
        }
    }

    private fun onConfirmPinError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    _confirmError.value = it.userMsg
                }
            }
            is NoConnectionException -> _showRetryDialog.postValue(null)
            else -> _technicalError.postValue(Unit)
        }
    }
}
