package com.telekom.citykey.view.auth_webview

import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.ausweiss_app.IdentConst
import com.telekom.citykey.domain.ausweiss_app.IdentInteractor
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy

class AuthBottomSheetDialogViewModel(
    private val identInteractor: IdentInteractor,
    private val url: String
) : BaseViewModel() {

    val newState = identInteractor.state
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()

    fun onFeatureReady() {
        identInteractor.startIdentification(url)
    }

    fun onAccessAccepted() {
        identInteractor.sendCMD(IdentConst.CMD_ACCEPT)
    }

    fun onSubmitPin(pin: String) {
        identInteractor.setPin(pin)
    }

    fun onSubmitCan(can: String) {
        identInteractor.setCan(can)
    }

    fun onSubmitPuk(puk: String) {
        identInteractor.setPuk(puk)
    }

    override fun onCleared() {
        identInteractor.unBind()
        super.onCleared()
    }
}
