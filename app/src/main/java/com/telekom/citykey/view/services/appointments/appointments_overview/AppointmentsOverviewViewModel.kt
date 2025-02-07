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

package com.telekom.citykey.view.services.appointments.appointments_overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.appointments.AppointmentsInteractor
import com.telekom.citykey.domain.services.appointments.AppointmentsState
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.appointments.Appointment
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class AppointmentsOverviewViewModel(
    private val appointmentsInteractor: AppointmentsInteractor,
    private val servicesInteractor: ServicesInteractor,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    val showErrorSnackbar: LiveData<Unit> get() = _showErrorSnackbar
    val showNoInternetDialog: LiveData<Unit> get() = _showNoInternetDialog
    val deletionSuccessful: LiveData<Boolean> get() = _deletionSuccessful
    val state: LiveData<AppointmentsState> get() = appointmentsInteractor.appointmentsState
    val appointmentsData: LiveData<List<Appointment>> get() = appointmentsInteractor.appointments
    val stopRefreshSpinner: LiveData<Boolean> get() = _stopRefreshSpinner
    val userLoggedOut: LiveData<Unit>
        get() = globalData.user
            .filter { it is UserState.Absent }
            .map { Unit }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
    val serviceIsAvailable: LiveData<Boolean>
        get() = servicesInteractor.state
            .filter { it !is ServicesStates.Loading }
            .map { state ->
                if (state is ServicesStates.Success) {
                    state.data.services.find { it.function == ServicesFunctions.TERMINE } != null
                } else false
            }
            .toLiveData()

    private val _showErrorSnackbar: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _showNoInternetDialog: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _deletionSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _stopRefreshSpinner: MutableLiveData<Boolean> = MutableLiveData()

    init {
        appointmentsInteractor.setAppointmentsRead()
    }

    fun onRefresh() {
        launch {
            appointmentsInteractor.refreshAppointments()
                .retryOnError(this::onRefreshError, retryDispatcher, pendingRetries, "APPT_REFRESH")
                .subscribe({}, this::onRefreshError)
        }
    }

    private fun onRefreshError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showRetryDialog.postValue(null)
            else -> _stopRefreshSpinner.postValue(true)
        }
    }

    fun onUndoDeletion() {
        launch {
            appointmentsInteractor.restoreDeletedAppointments().observeOn(AndroidSchedulers.mainThread())
                .subscribe({ Timber.i("Last appointment restored !") }, this::onSwipeGestureNetworkError)
        }
    }

    fun onDelete(appointments: Appointment) {
        launch {
            appointmentsInteractor.deleteAppointments(appointments).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Timber.i("Appointment with UUID = ${appointments.apptId} deleted !")
                        _deletionSuccessful.postValue(true)
                    },
                    this::onSwipeGestureNetworkError
                )
        }
    }

    private fun onSwipeGestureNetworkError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showNoInternetDialog.call()
            else -> _showErrorSnackbar.call()
        }
    }
}
