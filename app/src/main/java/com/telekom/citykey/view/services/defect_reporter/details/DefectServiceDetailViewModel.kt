package com.telekom.citykey.view.services.defect_reporter.details

import androidx.lifecycle.*
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.defect_reporter.DefectReporterInteractor
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class DefectServiceDetailViewModel(
    private val defectReporterInteractor: DefectReporterInteractor
) : NetworkingViewModel() {
    val defectCategoryAvailable: LiveData<Unit> get() = _defectCategoryAvailable
    private val _defectCategoryAvailable: MutableLiveData<Unit> = SingleLiveEvent()

    fun onOpenDefectReporterClicked() {
        launch {
            defectReporterInteractor.loadCategories()
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "defectCat")
                .subscribe({ _defectCategoryAvailable.value = Unit }, this::onError)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> {
                showRetry()
            }
            else -> {
                _technicalError.value = Unit
            }
        }
    }
}
