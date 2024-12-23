package com.telekom.citykey.view.services.appointments.qr

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.utils.QRUtils
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AppointmentQRViewModel(
    private val globalData: GlobalData
) : BaseViewModel() {

    val qrBitmap: LiveData<Bitmap> get() = _qrBitmap
    val userLoggedOut: LiveData<Unit> get() = globalData.user
        .filter { it is UserState.Absent }
        .map { Unit }
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()

    private val _qrBitmap: MutableLiveData<Bitmap> = MutableLiveData()

    fun onViewCreated(uuid: String) {
        launch {
            Single.just(uuid)
                .subscribeOn(Schedulers.io())
                .map(QRUtils::generateQRBitmap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_qrBitmap::postValue, Timber::e)
        }
    }
}
