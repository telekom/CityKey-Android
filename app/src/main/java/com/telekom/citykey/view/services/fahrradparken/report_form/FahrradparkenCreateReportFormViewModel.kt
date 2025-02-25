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

package com.telekom.citykey.view.services.fahrradparken.report_form

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.OscaLocationManager
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.fahrradparken.FahrradparkenServiceInteractor
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.FahrradparkenRequest
import com.telekom.citykey.models.defect_reporter.DefectSuccess
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.Validation
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FahrradparkenCreateReportFormViewModel(
    private val globalData: GlobalData,
    private val userInteractor: UserInteractor,
    private val locationManager: OscaLocationManager,
    private val fahrradparkenServiceInteractor: FahrradparkenServiceInteractor
) : NetworkingViewModel() {
    val inputValidation: LiveData<Unit> get() = _inputValidation
    private val _inputValidation: MutableLiveData<Unit> = MutableLiveData()

    val reportSubmitted: LiveData<DefectSuccess> get() = _reportSubmitted
    private val _reportSubmitted: MutableLiveData<DefectSuccess> = SingleLiveEvent()
    val reportSubmissionError: LiveData<String> get() = _reportSubmissionError
    private val _reportSubmissionError: SingleLiveEvent<String> = SingleLiveEvent()

    val userEmail: LiveData<String> get() = _userEmail
    private val _userEmail: MutableLiveData<String> = SingleLiveEvent()

    val cityName: LiveData<String> get() = MutableLiveData(globalData.cityName)

    init {
        launch {
            userInteractor.user
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it is UserState.Present) {
                        _userEmail.postValue(it.profile.email)
                    }
                }
        }
    }

    fun onSendReportClicked(
        email: String,
        firstName: String,
        lastName: String,
        yourConcern: String,
        subServiceCode: String,
        serviceCode: String,
        latLng: LatLng,
        image: Bitmap?,
        url: String = "",
    ) {
        if (email.isEmpty() || Validation.isEmailFormat(email)) {
            launch {
                locationManager.getAddressFromLatLng(latLng.latitude, latLng.longitude)
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap {
                        val fahrradparkenRequest = FahrradparkenRequest(
                            serviceCode = serviceCode,
                            subServiceCode = subServiceCode,
                            mediaUrl = url,
                            email = email,
                            description = yourConcern,
                            lat = latLng.latitude.toString(),
                            long = latLng.longitude.toString(),
                            firstName = firstName,
                            lastName = lastName
                        )
                        fahrradparkenServiceInteractor.createFahrradparkenReport(fahrradparkenRequest, image)
                    }
                    .retryOnError(this::onError, retryDispatcher, pendingRetries)
                    .subscribe(_reportSubmitted::postValue, this::onError)
            }
        } else
            _inputValidation.postValue(Unit)
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> {
                showRetry()
            }

            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.DUPLICATE_DEFECT,
                        ErrorCodes.DEFECT_OUTSIDE_CITY,
                        ErrorCodes.DEFECT_ALREADY_REPORTED,
                        ErrorCodes.MULTIPLE_DEFECT_ALREADY_REPORTED,
                        ErrorCodes.DEFECT_IMAGE_TOO_LARGE_REPORTED -> {
                            _reportSubmissionError.postValue(it.userMsg)
                        }

                        else -> {
                            _technicalError.value = Unit
                        }
                    }
                }
            }

            else -> {
                _technicalError.value = Unit
            }
        }
    }

}
