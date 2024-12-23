package com.telekom.citykey.view.user.profile.change_residence

import androidx.lifecycle.LiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.PersonalDetailChangeRequest
import com.telekom.citykey.models.user.ResidenceValidationResponse
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class ChangeResidenceViewModel(
    private val globalData: GlobalData,
    private val userRepository: UserRepository,
    private val userInteractor: UserInteractor
) : NetworkingViewModel() {

    companion object {
        private const val CHANGE_POSTAL_CODE_API_TAG = "Change_Postcode"
    }

    val saveSuccessful: LiveData<Boolean> get() = _saveSuccessful
    val requestSent: LiveData<ResidenceValidationResponse> get() = _requestSent
    val generalErrors: LiveData<Int> get() = _generalErrors
    val onlineErrors: LiveData<Pair<String?, String?>> get() = _onlineErrors
    val logUserOut: LiveData<Unit> get() = _logUserOut
    val inputValidation: SingleLiveEvent<Int> get() = _inputValidation

    private val _saveSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _inputValidation: SingleLiveEvent<Int> = SingleLiveEvent()
    private val _requestSent: SingleLiveEvent<ResidenceValidationResponse> = SingleLiveEvent()
    private val _generalErrors: SingleLiveEvent<Int> = SingleLiveEvent()
    private val _onlineErrors: SingleLiveEvent<Pair<String?, String?>> = SingleLiveEvent()
    private val _logUserOut: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onSaveClicked(postalCode: String) {
        if (postalCode.length == 5) {
            launch {
                userRepository.validatePostalCode(postalCode)
                    .retryOnError(this::onError, retryDispatcher, pendingRetries)
                    .subscribe(
                        {
                            _requestSent.value = it
                            savePostalCode(postalCode, it.cityName, it.homeCityId)
                        },
                        this::onError
                    )
            }
        } else {
            _inputValidation.value = R.string.r_001_registration_error_incorrect_postcode
        }
    }

    private fun savePostalCode(postalCode: String, newCityName: String, newCityId: Int) {
        launch {
            userRepository.changePersonalData(PersonalDetailChangeRequest("", postalCode), "postalCode")
                .retryOnError(this::onError, retryDispatcher, pendingRetries, CHANGE_POSTAL_CODE_API_TAG)
                .subscribe(
                    {
                        userInteractor.updatePersonalDataLocally(postalCode, newCityName, newCityId)
                        _saveSuccessful.postValue(true)
                    },
                    this::onError
                )
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is InvalidRefreshTokenException -> {
                globalData.logOutUser(throwable.reason)
                _logUserOut.postValue(Unit)
            }
            is NoConnectionException -> {
                _showRetryDialog.postValue(CHANGE_POSTAL_CODE_API_TAG)
            }
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.MULTIPLE_ERRORS -> _generalErrors.postValue(R.string.p_003_profile_email_change_technical_error)
                        ErrorCodes.CHANGE_POSTAL_CODE_INVALID, ErrorCodes.CHANGE_POSTAL_CODE_VALIDATION_ERROR -> _onlineErrors.postValue(
                            it.userMsg to null
                        )
                        else -> _technicalError.value = Unit
                    }
                }
            }
            else -> {
                _technicalError.value = Unit
            }
        }
    }
}
