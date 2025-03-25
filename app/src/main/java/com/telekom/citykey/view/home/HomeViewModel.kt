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

package com.telekom.citykey.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.city.events.EventsHomeData
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.city.events.EventsState
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.domain.city.weather.WeatherInteractor
import com.telekom.citykey.domain.city.weather.WeatherState
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.network.extensions.toHomeData
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.networkinterface.models.live_data.HomeData
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeViewModel(
    private val globalData: GlobalData,
    private val eventsInteractor: EventsInteractor,
    private val newsInteractor: NewsInteractor,
    private val weatherInteractor: WeatherInteractor,
    private val preferencesHelper: PreferencesHelper,
) : BaseViewModel() {

    val homeData: LiveData<HomeData> get() = _homeData
    val userState: LiveData<Boolean> get() = _userState
    val eventsHomeData: LiveData<EventsHomeData> get() = _eventsHomeData
    val eventsState: LiveData<EventsState> get() = eventsInteractor.state
    val refreshFinished: LiveData<Unit> get() = _refreshFinished
    val cityWeather: LiveData<WeatherState>
        get() = weatherInteractor.weatherData
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
    val cityNews: LiveData<NewsState>
        get() = newsInteractor.newsObservable
            .map(newsInteractor::mapContent)
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
    val citySelectionTooltipState: LiveData<Boolean> get() = _homeData.switchMap { _citySelectionTooltipState }

    private val _homeData: MutableLiveData<HomeData> = MutableLiveData()
    private val _userState: MutableLiveData<Boolean> = MutableLiveData()
    private val _events: MutableLiveData<List<Event>> = MutableLiveData()
    private val _favoredEvents: MutableLiveData<List<Event>?> = MutableLiveData()
    private val _eventsHomeData: MediatorLiveData<EventsHomeData> = MediatorLiveData()
    private val _refreshFinished: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _citySelectionTooltipState: SingleLiveEvent<Boolean> = SingleLiveEvent()

    init {
        observeCityDetails()
        launch { globalData.user.map { it is UserState.Present }.subscribe(_userState::postValue) }
        observeEvents()
        shouldShowCitySelectionToolTip()
    }

    private fun observeCityDetails() {
        launch {
            globalData.city
                .subscribeOn(Schedulers.io())
                .map { it.toHomeData() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_homeData::postValue)
        }
    }

    fun shouldUpdateWidget() = newsInteractor.shouldUpdateWidget

    fun widgetUpdateDone() = newsInteractor.updateWidgetDone()

    private fun observeEvents() {
        launch {
            eventsInteractor.events.map { it.take(eventsInteractor.homeEventsCount) }.subscribe(_events::postValue)
        }
        launch {
            eventsInteractor.favoredEvents
                .map { it.map { event -> event.copy() } }
                .map { it.sortedBy { event -> event.startDate } }
                .map { it.forEach { event -> event.isFavored = true }; return@map it }
                .map { it.take(eventsInteractor.homeFavoredEventsCount) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.isEmpty()) _favoredEvents.postValue(null)
                    else _favoredEvents.postValue(it)
                }
        }

        _eventsHomeData.addSource(_events) {
            _eventsHomeData.postValue(EventsHomeData(_events.value, _favoredEvents.value))
        }

        _eventsHomeData.addSource(_favoredEvents) {
            _eventsHomeData.postValue(EventsHomeData(_events.value, _favoredEvents.value))
        }
    }

    private fun shouldShowCitySelectionToolTip() {
        _citySelectionTooltipState.postValue(!preferencesHelper.getShowedCitySelectionToolTip())
    }

    fun onTooltipDismissed() {
        _citySelectionTooltipState.value = false
        preferencesHelper.saveShowedCitySelectionToolTip()
    }

    fun onRefresh() {
        launch {
            globalData.refreshContent()
                .doFinally { _refreshFinished.postValue(Unit) }
                .subscribe()
        }
    }

    fun isFtuInteractionCompleted() = preferencesHelper.isFirstTime.not()

}
