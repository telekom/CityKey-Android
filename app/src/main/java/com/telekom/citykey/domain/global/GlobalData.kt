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

package com.telekom.citykey.domain.global

import com.telekom.citykey.R
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.models.content.City
import com.telekom.citykey.view.user.login.LogoutReason
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber

class GlobalData(
    private val globalMessages: GlobalMessages,
    private val userInteractor: UserInteractor,
    private val cityInteractor: CityInteractor
) {

    val city: Observable<City> get() = cityInteractor.city
    val user: Observable<UserState> get() = userInteractor.user
    val unexpectedLogout get() = userInteractor.unexpectedLogout
    val currentCityId get() = cityInteractor.currentCityId
    val userId get() = userInteractor.userId
    val userCityName get() = userInteractor.userCityName
    val cityColor get() = cityInteractor.cityColor
    val cityName get() = cityInteractor.cityName
    val cityLocation get() = cityInteractor.cityLocation
    val isUserBrowsingHomeCity get() = userInteractor.isUserLoggedIn && userInteractor.userCityId == cityInteractor.currentCityId
    val isUserLoggedIn get() = userInteractor.isUserLoggedIn
    val hasUserAcceptedDpn get() = userInteractor.hasAcceptedDpn

    private var _shouldRefreshServices = false
    val shouldRefreshServices: Boolean
        get() = _shouldRefreshServices

    fun refreshContent(): Completable = cityInteractor.loadCity()
        .flatMapCompletable {
            if (userInteractor.isUserLoggedIn)
                userInteractor.updateUser().ignoreElement()
            else Completable.complete()
        }
        .doOnError {
            when (it) {
                is NoConnectionException ->
                    globalMessages.displayToast(R.string.dialog_no_internet)

                is InvalidRefreshTokenException ->
                    logOutUser(it.reason)

                else -> Timber.e(it)
            }
        }
        .onErrorComplete()

    fun loadCity(availableCity: AvailableCity) = cityInteractor.loadCity(availableCity)

    fun loadUser(isLoggedIn: Boolean = true): Completable =
        if (isLoggedIn) userInteractor.updateUser()
            .ignoreElement()
        else {
            userInteractor.setUserAbsent()
            Completable.complete()
        }

    fun logOutUser(logoutReason: LogoutReason = LogoutReason.TECHNICAL_LOGOUT) {
        userInteractor.logOutUser(logoutReason)
    }

    fun setServicesToReload() {
        _shouldRefreshServices = true
    }

    fun markGetServicesCompleted() {
        _shouldRefreshServices = false
    }
}
