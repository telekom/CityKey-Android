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

package com.telekom.citykey.view.user.profile.delete_account

import androidx.lifecycle.LiveData
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.networkinterface.models.api.requests.DeleteAccountRequest
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.user.login.LogoutReason

class DeleteAccountValidationViewModel(
    private val userRepository: UserRepository,
    private val globalData: GlobalData,
) : NetworkingViewModel() {

    companion object {
        private const val DELETE_API_TAG = "delete"
    }

    val error: LiveData<String> get() = _error
    val success: LiveData<Unit> get() = _success

    private val _error: SingleLiveEvent<String> = SingleLiveEvent()
    private val _success: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onConfirmClicked(password: String) {
        launch {
            userRepository.deleteUser(DeleteAccountRequest(password))
                .retryOnError(this::onError, retryDispatcher, pendingRetries, DELETE_API_TAG)
                .subscribe(
                    {
                        globalData.logOutUser(LogoutReason.NO_LOGOUT_REASON)
                        _success.call()
                    },
                    this::onError
                )
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showRetryDialog.postValue(DELETE_API_TAG)

            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.ACCOUNT_WRONG_PASSWORD -> _error.postValue(it.userMsg)
                        else -> _technicalError.value = Unit
                    }
                }
            }

            else -> _technicalError.value = Unit
        }
    }
}
