package com.telekom.citykey.view.user.profile.delete_account

import androidx.lifecycle.LiveData
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.DeleteAccountRequest
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.user.login.LogoutReason

class DeleteAccountValidationViewModel(
    private val userRepository: UserRepository,
    private val globalData: GlobalData,
) : NetworkingViewModel() {

    companion object {
        private const val DELETE_API_TAG = "delete"
    }

    val error: LiveData<String> get() = _error
    val success: LiveData<Unit> get() = _success

    private val _error: SingleLiveEvent<String> = SingleLiveEvent()
    private val _success: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onConfirmClicked(password: String) {
        launch {
            userRepository.deleteUser(DeleteAccountRequest(password))
                .retryOnError(this::onError, retryDispatcher, pendingRetries, DELETE_API_TAG)
                .subscribe(
                    {
                        globalData.logOutUser(LogoutReason.NO_LOGOUT_REASON)
                        _success.call()
                    },
                    this::onError
                )
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showRetryDialog.postValue(DELETE_API_TAG)

            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.ACCOUNT_WRONG_PASSWORD -> _error.postValue(it.userMsg)
                        else -> _technicalError.value = Unit
                    }
                }
            }
            else -> _technicalError.value = Unit
        }
    }
}
