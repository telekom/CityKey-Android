package com.telekom.citykey.view.services.service_detail_help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class ServiceDetailHelpViewModel(
    private val servicesInteractor: ServicesInteractor,
    private val serviceId: Int
) : NetworkingViewModel() {

    val info: LiveData<String?> get() = _info
    private val _info: MutableLiveData<String?> = MutableLiveData()

    init {
        getData()
    }

    fun onRetryClicked() {
        getData()
    }

    private fun getData() {
        launch {
            servicesInteractor.getInfo(serviceId)
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .map { it.helpText }
                .subscribe(_info::postValue, this::onError)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> {
                super.showRetry()
                _info.postValue(null)
            }
            else -> {
                _info.postValue(null)
                _technicalError.postValue(Unit)
            }
        }
    }
}
