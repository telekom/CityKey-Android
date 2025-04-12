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

package com.telekom.citykey.view.services.defect_reporter.report_form

import android.graphics.Bitmap
import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.OscaLocationManager
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.defect_reporter.DefectReporterInteractor
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.api.requests.DefectRequest
import com.telekom.citykey.networkinterface.models.defect_reporter.DefectSuccess
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.Validation
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DefectReportFormViewModel(
    private val locationManager: OscaLocationManager,
    private val defectReporterInteractor: DefectReporterInteractor,
    private val userInteractor: UserInteractor,
    private val globalData: GlobalData,
    private val preferencesHelper: PreferencesHelper
) : NetworkingViewModel() {
    val inputValidation: LiveData<Unit> get() = _inputValidation
    private val _inputValidation: MutableLiveData<Unit> = MutableLiveData()

    val defectSubmitted: LiveData<DefectSuccess> get() = _defectSubmitted
    private val _defectSubmitted: MutableLiveData<DefectSuccess> = SingleLiveEvent()
    val defectSubmissionError: LiveData<String> get() = _defectSubmissionError
    private val _defectSubmissionError: SingleLiveEvent<String> = SingleLiveEvent()

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
        wasteBinId: String,
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
                        val defectRequest = getDefectReportRequest(
                            email,
                            firstName,
                            lastName,
                            wasteBinId,
                            yourConcern,
                            subServiceCode,
                            serviceCode,
                            latLng,
                            url,
                            it
                        )
                        defectReporterInteractor.sendDefectRequest(defectRequest, image)
                    }
                    .retryOnError(this::onError, retryDispatcher, pendingRetries)
                    .subscribe(_defectSubmitted::postValue, this::onError)
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
                        ErrorCodes.DEFECT_OUTSIDE_CITY, ErrorCodes.DEFECT_ALREADY_REPORTED,
                        ErrorCodes.DEFECT_IMAGE_TOO_LARGE_REPORTED -> {
                            _defectSubmissionError.postValue(it.userMsg)
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

    private fun getDefectReportRequest(
        email: String,
        firstName: String,
        lastName: String,
        wasteBinId: String,
        yourConcern: String,
        subServiceCode: String,
        serviceCode: String,
        latLng: LatLng,
        url: String,
        address: Address?
    ): DefectRequest {
        return DefectRequest(
            firstName = firstName,
            lastName = lastName,
            wasteBinId = wasteBinId,
            serviceCode = serviceCode,
            lat = latLng.latitude.toString(),
            long = latLng.longitude.toString(),
            email = email,
            description = yourConcern,
            mediaUrl = url,
            subServiceCode = subServiceCode,
            houseNumber = address?.subThoroughfare.toString(),
            location = address?.getAddressLine(0).toString(),
            phoneNumber = "",
            postalCode = address?.postalCode.toString(),
            streetName = address?.thoroughfare.toString()
        )
    }

    fun isPreview() = preferencesHelper.isPreviewMode
}
