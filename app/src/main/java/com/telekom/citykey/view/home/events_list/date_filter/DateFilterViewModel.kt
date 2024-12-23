package com.telekom.citykey.view.home.events_list.date_filter

import androidx.lifecycle.LiveData
import com.telekom.citykey.custom.views.calendar.DateSelection
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.toApiFormat
import com.telekom.citykey.view.BaseViewModel
import timber.log.Timber

class DateFilterViewModel(
    private val eventsInteractor: EventsInteractor,
    globalData: GlobalData
) : BaseViewModel() {

    val dateSelection: LiveData<DateSelection?> get() = _dateSelection
    val color: LiveData<Int> get() = _color
    val eventsCount: LiveData<Int?> get() = eventsInteractor.eventsCount

    private val _dateSelection: SingleLiveEvent<DateSelection?> = SingleLiveEvent()
    private val _color: SingleLiveEvent<Int> = SingleLiveEvent()

    private var selection: DateSelection? = eventsInteractor.selectionDates.value

    init {
        _color.postValue(globalData.cityColor)
        _dateSelection.postValue(selection)

        if (eventsCount.value == null)
            launch {
                eventsInteractor.refreshEventsCount()
                    .subscribe(eventsInteractor::updateEventsCount, Timber::e)
            }
    }

    private fun refreshEventsCount() {
        launch {
            eventsInteractor.refreshEventsCount(
                endDate = selection?.end?.toApiFormat(),
                startDate = selection?.start?.toApiFormat()
            )
                .subscribe(eventsInteractor::updateEventsCount, Timber::e)
        }
    }

    fun onDateSelectedChange(selection: DateSelection?) {
        this.selection = selection
        refreshEventsCount()
    }

    fun revokeFiltering() {
        eventsInteractor.revokeEventsCount()
    }

    fun confirmFiltering() {
        eventsInteractor.updateDates(selection)
        eventsInteractor.applyFilters()
    }
}
