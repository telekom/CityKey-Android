package com.telekom.citykey.view.services.waste_calendar.service_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.toLiveData
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.content.CitizenService
import com.telekom.citykey.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers

class WasteCalendarDetailsViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor,
    private val servicesInteractor: ServicesInteractor,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    private val _wasteCalendarAvailable: MutableLiveData<Unit> = SingleLiveEvent()
    private val _launchFtu: MutableLiveData<Unit> = SingleLiveEvent()
    private val _appliedFilters: MutableLiveData<List<String>> = SingleLiveEvent()
    private val _categories: MutableLiveData<List<GetWasteTypeResponse>> = SingleLiveEvent()

    val wasteCalendarAvailable: LiveData<Unit> get() = _wasteCalendarAvailable
    val appliedFilters: LiveData<List<String>> get() = _appliedFilters
    val categories: LiveData<List<GetWasteTypeResponse>> get() = _categories

    val launchFtu: LiveData<Unit> get() = _launchFtu

    val service: LiveData<CitizenService?>
        get() = servicesInteractor.state
            .filter { it !is ServicesStates.Loading }
            .toLiveData()
            .map { state ->
                if (state is ServicesStates.Success)
                    state.data.services.find { it.function == ServicesFunctions.WASTE_CALENDAR }
                else
                    null
            }

    val userLoggedOut: LiveData<Unit>
        get() = globalData.user
            .filter { it is UserState.Absent }
            .map { Unit }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    fun onOpenWasteCalendarClicked() {
        launch {
            wasteCalendarInteractor.getData()
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "wasteData")
                .subscribe(
                    {
                        _wasteCalendarAvailable.postValue(Unit)
                    },
                    this::onError
                )
        }

    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> showRetry()
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.WASTE_CALENDAR_NO_ADDRESS,
                        ErrorCodes.WASTE_CALENDAR_NOT_FOUND,
                        ErrorCodes.WASTE_CALENDAR_WRONG_ADDRESS,
                        ErrorCodes.CALENDAR_NOT_EXIST -> _launchFtu.postValue(Unit)

                        else -> _technicalError.postValue(Unit)
                    }
                }
            }

            else -> _technicalError.postValue(Unit)
        }
    }

    fun getWasteCalendarFilterOptions() {
        launch {
            wasteCalendarInteractor.getFilterOptions()
                .map {
                    wasteCalendarInteractor.setCategoriesCount(it)
                    _categories.postValue(it)
                }
                .flatMap { wasteCalendarInteractor.getSelectedWastePickups() }
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        var applyfilter = ArrayList<String>()
                        it.wasteTypeIds.forEach {
                            applyfilter.add(it.toString())
                        }
                        _appliedFilters.postValue(applyfilter)
                        wasteCalendarInteractor.setFilters(applyfilter)
                    },
                    this::onError
                )
        }
    }

    fun checkSelectedPickupInFilterOptions() = wasteCalendarInteractor.filterCategories
}
