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

package com.telekom.citykey.view.services.waste_calendar.address_change

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.services.waste_calendar.WasteAddressState
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.waste_calendar.Address
import com.telekom.citykey.networkinterface.models.waste_calendar.FtuWaste
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.BackpressureStrategy

class WasteCalendarAddressViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    val wasteCalendarAvailable: LiveData<Unit> get() = _wasteCalendarAvailable
    val ftuAddress: LiveData<List<String>> get() = _ftuAddress
    val inputValidation: LiveData<Pair<String, FieldValidation>> get() = _inputValidation
    val error: LiveData<FieldValidation> get() = _error
    val cityData: LiveData<String> get() = MutableLiveData(globalData.cityName)
    val availableHouseNumbers: LiveData<List<String>> get() = _availableHouseNumbers
    val currentAddress: LiveData<Address> get() = _currentAddress
    val userLoggedOut: LiveData<Unit>
        get() = globalData.user
            .filter { it is UserState.Absent }
            .map { Unit }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    private val _availableHouseNumbers: MutableLiveData<List<String>> = SingleLiveEvent()
    private val _error: MutableLiveData<FieldValidation> = SingleLiveEvent()
    private val _inputValidation: MutableLiveData<Pair<String, FieldValidation>> = MutableLiveData()
    private val _ftuAddress: MutableLiveData<List<String>> = MutableLiveData()
    private val _wasteCalendarAvailable: MutableLiveData<Unit> = SingleLiveEvent()
    private val _currentAddress: MutableLiveData<Address> = MutableLiveData()

    private val houseNumberSuggestions = mutableListOf<String>()
    private val streetSuggestions = mutableListOf<FtuWaste>()
    private var streetName = ""
    private var houseNumber = ""

    init {
        _currentAddress.postValue(wasteCalendarInteractor.address)

        launch {
            wasteCalendarInteractor.ftuAddressSubject
                .subscribe {
                    when (it) {
                        is WasteAddressState.Error -> onError(it.throwable)
                        is WasteAddressState.Success -> updateList(it.items)
                    }
                }
        }
    }

    fun onOpenWasteCalendarClicked(streetName: String, houseNumber: String) {
        launch {
            wasteCalendarInteractor.getData(streetName, houseNumber)
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "wasteData")
                .subscribe(
                    {
                        _wasteCalendarAvailable.postValue(Unit)
                    },
                    this::onError
                )
        }
    }

    private fun updateList(list: List<FtuWaste>) {
        streetSuggestions.clear()
        streetSuggestions.addAll(list)
        _ftuAddress.postValue(list.map { it.streetName })
        validateStreetName(streetName)
    }

    fun onStreetTextChanged(selectedStreet: String, houseNumber: String) {
        this.houseNumber = houseNumber
        if (selectedStreet.isNotBlank()) {
            wasteCalendarInteractor.fetchAddressWaste(selectedStreet)
            streetName = selectedStreet
        }
    }

    private fun validateStreetName(streetName: String) {
        if (streetSuggestions.map { it.streetName.lowercase() }.contains(streetName.lowercase())) {
            _inputValidation.value = "streetName" to FieldValidation(FieldValidation.OK, null)
            houseNumberSuggestions.clear()
            streetSuggestions.find { it.streetName == streetName }?.let {
                houseNumberSuggestions.addAll(it.houseNumberList)
            }
            onHouseNumberChange(houseNumber)
            _availableHouseNumbers.postValue(houseNumberSuggestions)
        } else {
            _inputValidation.value = "streetName" to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.wc_004_ftu_street_error
            )
            _availableHouseNumbers.postValue(emptyList())
        }
    }

    fun onHouseNumberChange(houseNumber: String) {
        _inputValidation.value = "houseNumber" to when {
            houseNumber.isBlank() -> FieldValidation(FieldValidation.IDLE, null)
            houseNumberSuggestions.contains(houseNumber) -> FieldValidation(FieldValidation.OK, null)
            houseNumberSuggestions.isEmpty() -> FieldValidation(FieldValidation.IDLE, null)
            else -> FieldValidation(FieldValidation.ERROR, null, R.string.wc_004_ftu_house_error)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.CALENDAR_NOT_EXIST -> _error.postValue(
                            FieldValidation(
                                FieldValidation.ERROR,
                                it.userMsg
                            )
                        )

                        else -> _technicalError.postValue(Unit)
                    }
                }
            }

            else -> _technicalError.postValue(Unit)
        }
    }
}
