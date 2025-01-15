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

package com.telekom.citykey.view.user.profile.change_birthday

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.PersonalDetailChangeRequest
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.utils.extensions.toApiFormat
import com.telekom.citykey.view.NetworkingViewModel
import java.util.*

class ChangeBirthdayViewModel(
    private val userRepository: UserRepository,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    private val _logUserOut: MutableLiveData<Unit> = MutableLiveData()
    private val _validationError: MutableLiveData<FieldValidation> = MutableLiveData()
    private val _saveSuccessful: MutableLiveData<Unit> = MutableLiveData()

    val logUserOut: LiveData<Unit> get() = _logUserOut
    val validationError: LiveData<FieldValidation> get() = _validationError
    val saveSuccessful: LiveData<Unit> get() = _saveSuccessful

    private var birthDate: Date? = Date(System.currentTimeMillis())

    fun onBirthdaySelected(birthDate: Date?) {
        this.birthDate = birthDate
    }

    fun onSaveClicked() {
        launch {
            userRepository.changePersonalData(PersonalDetailChangeRequest(birthDate?.toApiFormat() ?: "", ""), "dob")
                .andThen(globalData.loadUser())
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "DOB")
                .subscribe(
                    {
                        _saveSuccessful.postValue(Unit)
                    },
                    this::onError
                )
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is InvalidRefreshTokenException -> {
                globalData.logOutUser(throwable.reason)
                _logUserOut.postValue(Unit)
            }
            is NoConnectionException -> {
                super.showRetry()
            }
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    _validationError.postValue(FieldValidation(FieldValidation.ERROR, it.userMsg))
                }
            }
            else -> {
                _technicalError.value = Unit
            }
        }
    }
}
