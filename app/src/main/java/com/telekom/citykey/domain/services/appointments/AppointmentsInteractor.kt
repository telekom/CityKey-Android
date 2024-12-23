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
