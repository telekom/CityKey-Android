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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.home.events_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.toLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.rxjava2.cachedIn
import com.telekom.citykey.R
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.city.events.EventsListItem
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi

class EventsListViewModel(
    private val eventsInteractor: EventsInteractor,
    private val globalData: GlobalData
) : BaseViewModel() {

    private val _cityColor: MutableLiveData<Int> = MutableLiveData()

    @ExperimentalCoroutinesApi
    val pagingData = eventsInteractor.eventsDataSource
        .cachedIn(viewModelScope)
        .toFlowable(BackpressureStrategy.DROP)
        .toLiveData()

    private val _favoredEvents: MutableLiveData<List<EventsListItem>> = MutableLiveData()

    val activeCategoryFilter = cityColor.switchMap { eventsInteractor.categoryFilters }
    val activeDateFilter = cityColor.switchMap { eventsInteractor.selectionDates }
    val favoredEvents = cityColor.switchMap { _favoredEvents }
    val cityColor: LiveData<Int> get() = _cityColor
    val clearLoadedEvents: LiveData<Unit> get() = eventsInteractor.clearLoadedEvents

    private val _loadStates: BehaviorSubject<CombinedLoadStates> = BehaviorSubject.create()

    init {
        launch {
            globalData.city
                .map { it.cityColorInt }
                .subscribe(_cityColor::postValue)
        }

        launch {
            eventsInteractor.favoredEvents
                .map { it.map { event -> event.copy() } }
                .map { it.sortedBy { event -> event.startDate } }
                .map { it.forEach { event -> event.isFavored = true }; return@map it }
                .map {
                    val list = mutableListOf<EventsListItem>()
                    if (it.isEmpty()) return@map list
                    list.add(EventsListItem.Header(R.string.h_001_events_favorites_header))
                    list.addAll(it.map { event -> EventsListItem.EventItem(event) })
                    list.add(EventsListItem.Header(R.string.h_001_events_header))
                    return@map list
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_favoredEvents::postValue)
        }

        launch {
            eventsInteractor.favoritesErrors
                .subscribe {
                    when (it) {
                        is InvalidRefreshTokenException -> {
                            globalData.logOutUser(it.reason)
                        }
                    }
                }
        }
    }

    fun onLoadStateChanged(states: CombinedLoadStates) {
        _loadStates.onNext(states)
    }
}
