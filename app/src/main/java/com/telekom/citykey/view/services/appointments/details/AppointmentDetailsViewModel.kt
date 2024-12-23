package com.telekom.citykey.view.services.appointments.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.OscaLocationManager
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.appointments.AppointmentsInteractor
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.appointments.Appointment
import com.telekom.citykey.models.appointments.Location
import com.telekom.citykey.utils.QRUtils
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AppointmentDetailsViewModel(
    private val locationManager: OscaLocationManager,
    private val servicesInteractor: ServicesInteractor,
    private val globalData: GlobalData,
    private val appointmentsInteractor: AppointmentsInteractor
) : NetworkingViewModel() {

    val showErrorSnackbar: LiveData<Unit> get() = _showErrorSnackbar
    val showNoInternetDialog: LiveData<Unit> get() = _showNoInternetDialog
    val cancelSuccessful: LiveData<Boolean> get() = _cancelSuccessful
    val deletionSuccessful: LiveData<Boolean> get() = _deletionSuccessful
    val cancellationAllowed: LiveData<Boolean> get() = _cancellationAllowed
    val latLng: LiveData<LatLng?> get() = _latLng
    val image: LiveData<String> get() = _image
    val qrBitmap: LiveData<Bitmap> get() = _qrBitmap
    val userLoggedOut: LiveData<Unit>
        get() = globalData.user
            .filter { it is UserState.Absent }
            .map { Unit }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    private val _showErrorSnackbar: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _showNoInternetDialog: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _deletionSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _cancelSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _latLng: MutableLiveData<LatLng?> = MutableLiveData()
    private val _image: MutableLiveData<String> = MutableLiveData()
    private val _qrBitmap: SingleLiveEvent<Bitmap> = SingleLiveEvent()
    private val _cancellationAllowed: MutableLiveData<Boolean> = MutableLiveData()

    init {
        launch {
            servicesInteractor.state
                .map { state ->
                    state is ServicesStates.Success &&
                        state.data.services
                        .find { it.function == ServicesFunctions.TERMINE }
                        ?.serviceParams?.get("action_cancel") == "NOT REQUIRED"
                }
                .subscribe(_cancellationAllowed::postValue)
        }
    }

    fun onViewCreated(appointment: Appointment) {
        getLatLngFromAddress(appointment.location)
        getServiceImage()
    }

    fun onShareClicked(uuid: String) {
        launch {
            Single.just(uuid)
                .subscribeOn(Schedulers.io())
                .map(QRUtils::generateQRBitmap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_qrBitmap::postValue, Timber::e)
        }
    }

    fun onCancelRequested(uuid: String) {
        launch {
            appointmentsInteractor.cancelAppointments(uuid)
                .retryOnError(this::onCancelError, retryDispatcher, pendingRetries, "CANCEL")
                .subscribe({ _cancelSuccessful.postValue(true) }, this::onCancelError)
        }
    }

    private fun onCancelError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showRetryDialog.postValue(null)
            else -> _cancelSuccessful.postValue(false)
        }
    }

    private fun getServiceImage() {
        launch {
            servicesInteractor.state
                .subscribeOn(Schedulers.io())
                .filter {
                    it is ServicesStates.Success
                }
                .map {
                    (it as ServicesStates.Success).data.services.find { service ->
                        service.function == "termine"
                    }?.image ?: ""
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_image::postValue)
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
            is NoConnectionException -> _showNoInternetDialog.postValue(Unit)
            else -> _showErrorSnackbar.call()
        }
    }

    private fun getLatLngFromAddress(location: Location) {
        val addressQuery = "${location.street} ${location.houseNumber}, ${location.postalCode} ${location.place}"
        launch {
            locationManager.getLatLngFromAddress(addressQuery)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_latLng::postValue) { _latLng.postValue(LatLng(0.0, 0.0)) }
        }
    }
}
