package com.telekom.citykey.view.dialogs.dpn_updates

import androidx.lifecycle.LiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class DpnUpdatesViewModel(
    private val oscaRepository: OscaRepository,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    val newDpnAccepted: LiveData<Unit> get() = _newDpnAccepted

    private val _newDpnAccepted: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onChangesAccepted() {
        launch {
            oscaRepository.acceptDataSecurityChanges(true)
                .andThen(globalData.loadUser())
                .retryOnError(::onError, retryDispatcher, pendingRetries, "acceptDpn")
                .subscribe({ _newDpnAccepted.call() }, this::onError)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> showRetry()
            is InvalidRefreshTokenException -> globalData.logOutUser(throwable.reason)
            else -> _technicalError.call()
        }
    }
}
