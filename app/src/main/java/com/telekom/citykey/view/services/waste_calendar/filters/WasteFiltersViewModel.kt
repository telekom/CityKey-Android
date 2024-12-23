package com.telekom.citykey.view.services.waste_calendar.filters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.models.waste_calendar.SaveSelectedWastePickupRequest
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class WasteFiltersViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor
) : NetworkingViewModel() {

    private val _appliedFilters: MutableLiveData<List<String>> = MutableLiveData()
    private val _categories: MutableLiveData<List<GetWasteTypeResponse>> = MutableLiveData()
    private val _selectedPickupStatus: MutableLiveData<Boolean> = MutableLiveData()

    val appliedFilters: LiveData<List<String>> get() = _appliedFilters
    val categories: LiveData<List<GetWasteTypeResponse>> get() = _categories
    val selectedPickupStatus: LiveData<Boolean> get() = _selectedPickupStatus

    init {
        launch {
            wasteCalendarInteractor.getFilterOptions()
                .map {
                    wasteCalendarInteractor.setCategoriesCount(it)
                    _categories.postValue(it)
                }
                .flatMap {
                    wasteCalendarInteractor.getSelectedWastePickups()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        var applyfilter = ArrayList<String>()
                        it.wasteTypeIds.forEach {
                            applyfilter.add(it.toString())
                        }
                        _appliedFilters.postValue(applyfilter)
                        wasteCalendarInteractor.appliyFilter(applyfilter)
                    },
                    this::onError
                )
        }
    }

    fun saveSelectedWastePickup(saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest) {
        launch {
            wasteCalendarInteractor.saveSelectedWastePickups(saveSelectedWastePickupRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        _selectedPickupStatus.postValue(it.isSuccessful)
                    },
                    this::onError
                )
        }
    }

    fun onCategoryFiltersAccepted(filters: List<String>) {
        var wasteTypeIds = arrayListOf<Int>()
        filters.forEach {
            wasteTypeIds.add(it.toInt())
        }
        saveSelectedWastePickup(SaveSelectedWastePickupRequest(wasteTypeIds))
        wasteCalendarInteractor.setFilters(filters)
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> showRetry()
            else -> _technicalError.postValue(Unit)
        }
    }
}
