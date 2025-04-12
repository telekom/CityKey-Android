/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.domain.city.events

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.observable
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.custom.views.calendar.DateSelection
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.content.City
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.networkinterface.models.content.EventCategory
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.toApiFormat
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class EventsInteractor(
    private val cityRepository: CityRepository,
    private val globalMessages: GlobalMessages,
    private val globalData: GlobalData
) {

    val state: LiveData<EventsState> get() = _state
    val selectionDates: LiveData<DateSelection?> get() = _selectionDates
    val categoryIdFilters: LiveData<ArrayList<Int>?> get() = _categoryIdFilters
    val categoryFilters: LiveData<ArrayList<EventCategory>?> get() = _categoryFilters
    val eventsCount: LiveData<Int?> get() = _eventsCount
    val events: Observable<List<Event>> get() = _events.hide()
    val favoredEvents: Observable<List<Event>>
        get() = _favoredEventsObservable.hide()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { if (!getFavoritesSuccess) refreshFavoredEvents() }
    val favoritesErrors: Observable<Throwable>
        get() =
            _favoritesErrors.hide()
                .onErrorReturn { it }

    var homeEventsCount = 0
        private set
    var homeFavoredEventsCount = 0
        private set

    private val _events: BehaviorSubject<List<Event>> = BehaviorSubject.create()
    private val _favoredEventsObservable: BehaviorSubject<List<Event>> =
        BehaviorSubject.createDefault(emptyList())
    private val _favoritesErrors: PublishSubject<Throwable> = PublishSubject.create()
    private var _favoredEvents: MutableList<Event> = mutableListOf()
    private val _state: MutableLiveData<EventsState> = MutableLiveData()
    private val _selectionDates: MutableLiveData<DateSelection?> = MutableLiveData()
    private val _categoryIdFilters: MutableLiveData<ArrayList<Int>?> = MutableLiveData()
    private val _categoryFilters: MutableLiveData<ArrayList<EventCategory>?> = MutableLiveData()
    private val _eventsCount: MutableLiveData<Int?> = MutableLiveData()

    private val _clearLoadedEvents: MutableLiveData<Unit> = SingleLiveEvent()
    val clearLoadedEvents: LiveData<Unit> get() = _clearLoadedEvents

    val eventsDataSource: Observable<PagingData<Event>> =
        Pager(
            PagingConfig(
                pageSize = 20,
                initialLoadSize = 20
            )
        ) {
            EventsPagingSource(
                cityRepository = cityRepository,
                globalData = globalData,
                categoryFilters = _categoryIdFilters.value,
                selectedDates = _selectionDates.value
            ).also { eventsPagingDataSource = it }
        }.observable

    private var initialEventsDisposable: Disposable? = null
    private var eventsCountDisposable: Disposable? = null
    private var eventsFavoritesDisposable: Disposable? = null

    private var getFavoritesSuccess = false
    private var eventsPagingDataSource: EventsPagingSource? = null

    init {
        observeUserAndCity()
    }

    @SuppressLint("CheckResult")
    private fun observeUserAndCity() {
        globalData.city
            .doOnNext(this::refreshEvents)
            .distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
            .doOnNext {
                _events.onNext(emptyList())
                _favoredEvents = mutableListOf<Event>().also(_favoredEventsObservable::onNext)
                refreshFavoredEvents()
                _state.postValue(EventsState.FORCELOADING)
                _clearLoadedEvents.value = Unit
                eventsPagingDataSource?.invalidate()
            }
            .subscribe { clearFilters() }

        globalData.user.map { it is UserState.Present }.subscribe {
            if (globalData.currentCityId != -1)
                refreshFavoredEvents()
        }
    }

    fun updateDates(dateSelection: DateSelection?) {
        _selectionDates.value = dateSelection
    }

    fun updateCategories(
        categoryIdFilters: ArrayList<Int>?,
        categoryFilters: ArrayList<EventCategory>?
    ) {
        _categoryIdFilters.value = categoryIdFilters
        _categoryFilters.value = categoryFilters
    }

    fun revokeEventsCount() {
        _eventsCount.value = null
    }

    fun updateEventsCount(value: Int) {
        _eventsCount.value = value
    }

    private fun clearFilters() {
        _selectionDates.value = null
        _categoryIdFilters.value = null
        _categoryFilters.value = null
        _eventsCount.value = null
    }

    private fun refreshEvents(city: City, eventId: String? = "0") {
        initialEventsDisposable?.dispose()

        homeEventsCount = city.cityConfig?.eventsCount ?: 0
        homeFavoredEventsCount = city.cityConfig?.yourEventsCount ?: 0

        initialEventsDisposable = cityRepository.getEvents(cityId = city.cityId, pageNo = 1, eventId = eventId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _state.postValue(EventsState.RUNNING) }
            .doOnSuccess {
                if (it.isNotEmpty()) _state.postValue(EventsState.SUCCESS)
                else _state.postValue(EventsState.EMPTY)
            }
            .subscribe(_events::onNext, this::onError)
    }

    private fun refreshFavoredEvents() {
        if (!globalData.isUserLoggedIn) {
            eventsFavoritesDisposable?.dispose()
            getFavoritesSuccess = true
            _favoredEvents = mutableListOf<Event>().also(_favoredEventsObservable::onNext)
            return
        }

        eventsFavoritesDisposable = cityRepository.getFavoredEvents(cityId = globalData.currentCityId)
            .map { it.toMutableList() }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { getFavoritesSuccess = true }
            .doOnError { getFavoritesSuccess = false }
            .subscribe(
                { _favoredEvents = it.also(_favoredEventsObservable::onNext) },
                _favoritesErrors::onNext
            )
    }

    fun setEventFavored(isFavored: Boolean, event: Event): Completable =
        cityRepository.setEventFavored(isFavored, event.uid, globalData.currentCityId)
            .doOnComplete { storeFavoredEvent(isFavored, event) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { globalMessages.displayToast(R.string.a_message_event_favored_error) }

    private fun storeFavoredEvent(isFavored: Boolean, event: Event) {
        if (isFavored) {
            if (_favoredEvents.count { it.uid == event.uid } == 0) _favoredEvents.add(event)
        } else {
            _favoredEvents.find { it.uid == event.uid }?.let { _favoredEvents.remove(it) }
        }
        _favoredEventsObservable.onNext(_favoredEvents)
    }

    fun refreshEventsCount(
        startDate: String? = selectionDates.value?.start?.toApiFormat(),
        endDate: String? = selectionDates.value?.end?.toApiFormat(),
        categories: ArrayList<Int>? = categoryIdFilters.value
    ): Maybe<Int> = cityRepository.getEventsCount(
        cityId = globalData.currentCityId,
        start = startDate,
        end = endDate,
        categories = categories
    )
        .doOnSubscribe { eventsCountDisposable?.dispose(); eventsCountDisposable = it }
        .observeOn(AndroidSchedulers.mainThread())

    fun applyFilters() {
        _clearLoadedEvents.value = Unit
        eventsPagingDataSource?.invalidate()
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.ACTION_NOT_AVAILABLE -> {
                            _state.postValue(EventsState.ERRORACTION)
                        }

                        else -> {
                            _state.postValue(EventsState.FAILED)
                        }
                    }
                }
            }
        }
    }

    fun getEventDetails(eventId: String) =
        cityRepository.getEvents(cityId = globalData.currentCityId, pageNo = 1, eventId = eventId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _state.postValue(EventsState.RUNNING) }
            .doOnSuccess { if (it.isNotEmpty()) _state.postValue(EventsState.SUCCESS) else _state.postValue(EventsState.EMPTY) }

}
