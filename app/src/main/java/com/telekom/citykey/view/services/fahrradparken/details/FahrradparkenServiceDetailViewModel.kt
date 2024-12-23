package com.telekom.citykey.view.services.fahrradparken.details

import androidx.lifecycle.*
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.defect_reporter.DefectReporterInteractor
import com.telekom.citykey.domain.services.fahrradparken.FahrradparkenServiceInteractor
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class FahrradparkenServiceDetailViewModel(
    private val fahrradparkenServiceInteractor: FahrradparkenServiceInteractor
) : NetworkingViewModel() {
    val fahrradparkenCategoriesAvailable: LiveData<Unit> get() = _fahrradparkenCategoriesAvailable
    private val _fahrradparkenCategoriesAvailable: MutableLiveData<Unit> = SingleLiveEvent()

    fun onShowExistingReports() {
        launch {
            fahrradparkenServiceInteractor.loadCategories()
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "FahrradparkenCategories")
                .subscribe({ _fahrradparkenCategoriesAvailable.value = Unit }, this::onError)
        }
    }

    fun onCreateNewReport() {
        launch {
            fahrradparkenServiceInteractor.loadCategories()
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "FahrradparkenCategories")
                .subscribe({ _fahrradparkenCategoriesAvailable.value = Unit }, this::onError)
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
