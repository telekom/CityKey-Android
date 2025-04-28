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

package com.telekom.citykey.view.home.events_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.R
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.OscaLocationManager
import com.telekom.citykey.data.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.track.AnalyticsParameterKey
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.content.City
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class EventDetailsViewModel(
    private val locationManager: OscaLocationManager,
    private val eventsInteractor: EventsInteractor,
    private val globalData: GlobalData,
    private val preferencesHelper: PreferencesHelper,
    private val adjustManager: AdjustManager
) : NetworkingViewModel() {

    val latLng: LiveData<LatLng?> get() = _latLng
    val favored: LiveData<Boolean> get() = _favored
    val userLoggedIn: LiveData<Boolean> get() = _userLoggedIn
    val promptLoginRequired: LiveData<Boolean> get() = _promptLoginRequired
    val showFavoritesLoadError: LiveData<Unit> get() = _showFavoritesLoadError
    val events: LiveData<List<Event>> get() = _events
    val newCity: LiveData<City> get() = _newCity

    private val _latLng: MutableLiveData<LatLng?> = MutableLiveData()
    private val _favored: MutableLiveData<Boolean> = MutableLiveData()
    private val _userLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    private val _promptLoginRequired: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _showFavoritesLoadError: MutableLiveData<Unit> = MutableLiveData()
    private val _events: MutableLiveData<List<Event>> = MutableLiveData()
    private val _newCity: MutableLiveData<City> = MutableLiveData()

    init {

        launch {
            globalData.user.subscribe { _userLoggedIn.postValue(it is UserState.Present) }
        }

        launch {
            globalData.city
                .distinctUntilChanged { val1, val2 -> val1.cityId == val2.cityId }
                .subscribe(_newCity::postValue)
        }
    }

    fun onViewCreated(event: Event) {
        if (event.latitude != 0.0 && event.longitude != 0.0) {
            _latLng.postValue(LatLng(event.latitude, event.longitude))
        } else if (!event.locationAddress.isNullOrBlank()) {
            getLatLngFromAddress(event.locationAddress.orEmpty())
        } else {
            _latLng.postValue(null)
        }

        launch {
            eventsInteractor.favoredEvents
                .map { it.count { e -> e.uid == event.uid } != 0 }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_favored::postValue, Timber::e)
        }

        launch {
            eventsInteractor.favoritesErrors
                .subscribe {
                    when (it) {
                        is InvalidRefreshTokenException -> {
                            globalData.logOutUser(it.reason)
                        }

                        else -> _showFavoritesLoadError.postValue(Unit)
                    }
                }
        }
    }

    private fun getLatLngFromAddress(address: String) {
        launch {
            locationManager.getLatLngFromAddress(address)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_latLng::postValue) { _latLng.postValue(null) }
        }
    }

    fun onFavoriteClicked(isFavored: Boolean, event: Event) {
        launch {
            eventsInteractor.setEventFavored(isFavored, event)
                .doOnError(this::onError)
                .subscribe(
                    { _favored.postValue(isFavored) },
                    { _favored.postValue(!isFavored) }
                )
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is InvalidRefreshTokenException -> {
                globalData.logOutUser(throwable.reason)
                _promptLoginRequired.postValue(true)
            }

            is NoConnectionException -> _showRetryDialog.call()
            else -> _technicalError.value = Unit
        }
    }

    fun getEventsDetails(eventId: String) {
        launch {
            eventsInteractor.getEventDetails(eventId).subscribe(_events::postValue, this::onError)
        }
    }

    fun getDeepLinkCity() = preferencesHelper.getDeepLinkCityId()

    fun trackAdjustEngagement(eventId: String, engagementOption: String) {
        adjustManager.trackEvent(
            R.string.event_engagement,
            mapOf(
                AnalyticsParameterKey.eventId to eventId,
                AnalyticsParameterKey.eventEngagementOption to engagementOption
            )
        )
    }
}
