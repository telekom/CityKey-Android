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

package com.telekom.citykey.view.user.profile.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.content.UserProfile
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.BaseViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import io.reactivex.android.schedulers.AndroidSchedulers

class ProfileViewModel(
    private val globalData: GlobalData,
    private val userInteractor: UserInteractor,
    private val availableCitiesInteractor: AvailableCitiesInteractor,
) : BaseViewModel() {

    private val _profileContent: MutableLiveData<UserProfile> = MutableLiveData()
    val profileContent: LiveData<UserProfile> get() = _profileContent

    private val _logOutUser: SingleLiveEvent<Unit> = SingleLiveEvent()
    val logOutUser: LiveData<Unit> get() = _logOutUser

    private val _previewModeToggleFailed: MutableLiveData<Long> = SingleLiveEvent<Long>()
    val previewModeToggleFailed: LiveData<Long> get() = _previewModeToggleFailed

    init {
        observeProfile()
    }

    private fun observeProfile() =
        launch {
            userInteractor.user
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it is UserState.Present) {
                        _profileContent.value = it.profile
                    } else {
                        refreshDataPostLogoutAndUpdateUI()
                    }
                }
        }

    private fun refreshDataPostLogoutAndUpdateUI() {
        _refreshStarted.postValue(Unit)
        launch {
            availableCitiesInteractor.clearAvailableCities()
            availableCitiesInteractor.availableCities
                .map { availableCities ->
                    val userSelectedCityId = userInteractor.selectedCityId
                    if (userSelectedCityId == -1) {
                        availableCities.first()
                    } else {
                        availableCities.firstOrNull { it.cityId == userSelectedCityId } ?: availableCities.first()
                    }
                }
                .flatMap(globalData::loadCity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _logOutUser.value = Unit },
                    { _logOutUser.value = Unit }
                )
        }
    }

    fun onLogoutBtnClicked() {
        userInteractor.logOutUser(LogoutReason.ACTIVE_LOGOUT)
    }

    fun isCspUser(): Boolean = _profileContent.value?.isCspUser ?: false

    fun isPreviewMode(): Boolean = userInteractor.isPreviewMode()

    fun togglePreviewMode(shouldEnable: Boolean) {
        if (isPreviewMode() != shouldEnable) {
            performDataRefresh(shouldEnable)
        }
    }

    private val _refreshStarted: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _refreshFinished: SingleLiveEvent<Unit> = SingleLiveEvent()

    val refreshStarted: LiveData<Unit> get() = _refreshStarted
    val refreshFinished: LiveData<Unit> get() = _refreshFinished

    private fun performDataRefresh(shouldEnable: Boolean) {
        _refreshStarted.postValue(Unit)
        launch {
            userInteractor.togglePreviewMode(shouldEnable)
            availableCitiesInteractor.clearAvailableCities()
            availableCitiesInteractor.availableCities
                .map { availableCities ->
                    val userSelectedCityId = userInteractor.selectedCityId
                    if (userSelectedCityId == -1) {
                        availableCities.first()
                    } else {
                        availableCities.firstOrNull { it.cityId == userSelectedCityId } ?: availableCities.first()
                    }
                }
                .flatMap(globalData::loadCity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { _refreshFinished.postValue(Unit) },
                    {
                        _previewModeToggleFailed.postValue(System.currentTimeMillis())
                        userInteractor.togglePreviewMode(shouldEnable.not())
                        _refreshFinished.postValue(Unit)
                    }
                )
        }
    }
}
