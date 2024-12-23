package com.telekom.citykey.view.home.events_list.category_filter

import androidx.lifecycle.LiveData
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.content.EventCategory
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import timber.log.Timber

class CategoryFilterViewModel(
    private val globalData: GlobalData,
    private val cityRepository: CityRepository,
    private val eventsInteractor: EventsInteractor
) : NetworkingViewModel() {
    val allCategories: LiveData<List<EventCategory>> get() = _allCategories
    val filters: LiveData<ArrayList<Int>> get() = _filters
    val eventsCount: LiveData<Int?> get() = eventsInteractor.eventsCount

    private val _allCategories: SingleLiveEvent<List<EventCategory>> = SingleLiveEvent()
    private val _filters: SingleLiveEvent<ArrayList<Int>> = SingleLiveEvent()

    private var selectedCategories: ArrayList<Int> = arrayListOf()

    init {

        launch {
            cityRepository.getAllEventCategories(globalData.currentCityId)
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        _allCategories.postValue(it)
                        loadFilters()
                        if (eventsCount.value == null) refreshEventsCount()
                    },
                    this::onError
                )
        }
    }

    private fun loadFilters() {
        _filters.postValue(eventsInteractor.categoryIdFilters.value)
    }

    private fun refreshEventsCount() {
        launch {
            eventsInteractor.refreshEventsCount(categories = selectedCategories)
                .subscribe(eventsInteractor::updateEventsCount, Timber::e)
        }
    }

    fun onCategoryAdded(id: Int) {
        selectedCategories.add(id)
        refreshEventsCount()
    }

    fun onCategoryRemoved(id: Int) {
        selectedCategories.remove(id)
        refreshEventsCount()
    }

    fun onFiltersCleared() {
        selectedCategories.clear()
        refreshEventsCount()
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> {
                showRetry()
            }
            else -> _filters.postValue(arrayListOf())
        }
    }

    fun revokeFiltering() {
        eventsInteractor.revokeEventsCount()
    }

    fun confirmFiltering() {
        val eventsCategories = arrayListOf<EventCategory>()
        selectedCategories.forEach { id ->
            allCategories.value?.find {
                it.id == id
            }?.let { category -> eventsCategories.add(category) }
        }

        eventsInteractor.updateCategories(selectedCategories, eventsCategories)
        eventsInteractor.applyFilters()
    }
}
