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

package com.telekom.citykey.domain.services.appointments

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.models.appointments.Appointment
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AppointmentsInteractor(
    private val servicesRepository: ServicesRepository,
    private val globalData: GlobalData,
    private val servicesInteractor: ServicesInteractor,
    private val inAppNotificationsInteractor: InAppNotificationsInteractor
) {
    val appointments: MutableLiveData<List<Appointment>> = MutableLiveData()
    val appointmentsState: MutableLiveData<AppointmentsState> = MutableLiveData(AppointmentsState.Loading)
    private var lastDeletedAppointment: Appointment? = null
    private var setAppointmentsReadDisposable: Disposable? = null

    init {
        observeAppData()
    }

    @SuppressLint("CheckResult")
    private fun observeAppData() {
        globalData.city
            .distinctUntilChanged { v1, v2 -> v1.cityId == v2.cityId }
            .map { emptyList<Appointment>() }
            .subscribe(appointments::postValue)

        servicesInteractor.state
            .filter { it !is ServicesStates.Loading }
            .map {
                it is ServicesStates.Success &&
                    it.data.services.find { service -> service.function == ServicesFunctions.TERMINE } != null &&
                    globalData.isUserLoggedIn
            }
            .switchMap { isServiceAvailable ->
                return@switchMap if (isServiceAvailable) {
                    servicesRepository.getAppointments(globalData.userId!!, globalData.currentCityId)
                        .doOnSuccess { appointments ->
                            appointmentsState.postValue(
                                if (appointments.isEmpty()) AppointmentsState.Empty
                                else AppointmentsState.Success
                            )
                            setupUpdates(appointments.filter { !it.isRead }.size)
                        }
                        .onErrorReturn {
                            appointmentsState.postValue(AppointmentsState.Error)
                            setupUpdates(0)
                            emptyList()
                        }
                        .toFlowable()
                } else {
                    //TODO: Implement AppointmentsState.ServiceNotAvailable and handle UserNotLoggedIn separately
                    appointmentsState.postValue(AppointmentsState.UserNotLoggedIn)
                    setupUpdates(0)
                    Flowable.just(emptyList())
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(appointments::setValue, Timber::e)
    }

    fun refreshAppointments(): Completable =
        if (globalData.isUserLoggedIn) {
            servicesRepository.getAppointments(globalData.userId!!, globalData.currentCityId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    when (it) {
                        is InvalidRefreshTokenException -> {
                            globalData.logOutUser(it.reason)
                            appointments.postValue(emptyList())
                            setupUpdates(0)
                            appointmentsState.postValue(AppointmentsState.UserNotLoggedIn)
                        }
                        else -> {
                            if (appointmentsState.value != AppointmentsState.Success)
                                appointmentsState.postValue(AppointmentsState.Error)
                        }
                    }
                }
                .doOnSuccess { appointments ->
                    appointmentsState.postValue(
                        if (appointments.isEmpty()) AppointmentsState.Empty
                        else AppointmentsState.Success
                    )
                    if (appointments.any { !it.isRead }) setAppointmentsRead()
                    setupUpdates(0)
                }
                .doOnSuccess(appointments::postValue)
                .ignoreElement()
        } else {
            appointmentsState.postValue(AppointmentsState.UserNotLoggedIn)
            Completable.complete()
        }

    fun deleteAppointments(appointment: Appointment): Completable =
        servicesRepository.deleteAppointments(true, appointment.apptId, globalData.currentCityId)
            .doOnSubscribe {
                lastDeletedAppointment = appointment

                appointments.value?.toMutableList()?.let {
                    it.remove(appointment)
                    appointments.postValue(it)
                    setupUpdates()
                }
            }.doOnError { error ->
                appointments.value?.toMutableList()?.let {
                    it.add(lastDeletedAppointment!!)
                    appointments.postValue(it.sortedBy { info -> info.startTime })
                    setupUpdates()
                }
                when (error) {
                    is InvalidRefreshTokenException -> {
                        globalData.logOutUser(error.reason)
                        appointmentsState.postValue(AppointmentsState.UserNotLoggedIn)
                    }
                }
            }

    fun cancelAppointments(uuid: String): Completable = servicesRepository.cancelAppointments(uuid, globalData.currentCityId)
        .doOnComplete {
            appointments.value?.let { appts ->
                appts.find { it.uuid == uuid }?.apptStatus = Appointment.STATE_CANCELED
                appointments.postValue(appts)
            }
        }

    private fun setupUpdates(count: Int = appointments.value?.filter { !it.isRead }?.size ?: 0) {
        inAppNotificationsInteractor.setNotification(
            R.id.services_graph,
            ServicesFunctions.TERMINE,
            count
        )
    }

    fun restoreDeletedAppointments(): Completable =
        if (lastDeletedAppointment == null) Completable.complete()
        else servicesRepository.deleteAppointments(false, lastDeletedAppointment!!.apptId, globalData.currentCityId)
            .doOnSubscribe {
                appointments.value?.toMutableList()?.let {
                    it.add(lastDeletedAppointment!!)
                    appointments.postValue(it.sortedBy { info -> info.startTime })
                    setupUpdates()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())

    fun setAppointmentsRead() {
        appointments.value?.let { appts ->
            setAppointmentsReadDisposable?.dispose()
            setAppointmentsReadDisposable = Single.just(appts)
                .subscribeOn(Schedulers.io())
                .map { appts.filter { !it.isRead }.joinToString { appt -> appt.apptId } }
                .filter { it.isNotBlank() }
                .flatMapCompletable { servicesRepository.readAppointment(it, globalData.currentCityId) }
                .subscribe(
                    {
                        appts.forEach { it.isRead = true }
                        setupUpdates(0)
                    },
                    Timber::e
                )
        }
    }
}
