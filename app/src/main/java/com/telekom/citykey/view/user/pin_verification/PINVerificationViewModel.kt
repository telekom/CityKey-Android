package com.telekom.citykey.view.user.pin_verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.PinConfirmationRequest
import com.telekom.citykey.models.api.requests.ResendPinRequest
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.pin_verification.success.VerificationSuccess

class PINVerificationViewModel(private val userRepository: UserRepository, private val globalData: GlobalData) :
    NetworkingViewModel() {

    val emailResent: LiveData<Boolean> get() = _emailResent
    val resendError: LiveData<String> get() = _resendError
    val pinConfirmed: LiveData<Int> get() = _pinConfirmed
    val confirmError: LiveData<String> get() = _confirmError

    private val _emailResent: MutableLiveData<Boolean> = MutableLiveData()
    private val _resendError: SingleLiveEvent<String> = SingleLiveEvent()
    private val _confirmError: SingleLiveEvent<String> = SingleLiveEvent()
    private val _pinConfirmed: SingleLiveEvent<Int> = SingleLiveEvent()

    fun onResendEmailClicked(email: String, actionName: String) {
        launch {
            userRepository.resendPIN(ResendPinRequest(email.lowercase()), actionName = actionName)
                .retryOnError(this::onResendEmailError, retryDispatcher, pendingRetries, "callEmail")
                .subscribe({ _emailResent.postValue(true) }, ::onResendEmailError)
        }
    }

    fun onPinConfirmed(pin: String, email: String, actionName: String, actionType: Int) {
        launch {
            userRepository.confirmRegistration(PinConfirmationRequest(email.lowercase(), pin), actionName)
                .retryOnError(this::onConfirmPinError, retryDispatcher, pendingRetries, "confirmation")
                .subscribe(
                    {
                        _pinConfirmed.value = actionType
                        if (actionType == VerificationSuccess.EMAIL_CHANGED) globalData.logOutUser(LogoutReason.NO_LOGOUT_REASON)
                    },
                    ::onConfirmPinError
                )
        }
    }

    private fun onResendEmailError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                _resendError.value =
                    (throwable.error as OscaErrorResponse).errors.joinToString { it.userMsg }
            }
            is NoConnectionException -> _showRetryDialog.postValue(null)
            else -> _technicalError.postValue(Unit)
        }
    }

    private fun onConfirmPinError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    _confirmError.value = it.userMsg
                }
            }
            is NoConnectionException -> _showRetryDialog.postValue(null)
            else -> _technicalError.postValue(Unit)
        }
    }
}
