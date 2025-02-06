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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.main

import android.app.ActivityManager
import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.ausweiss_app.IdentInteractor
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.legal_data.LegalDataManager
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.UnsupportedVersionException
import com.telekom.citykey.domain.security.rootbeer.RootDetectedException
import com.telekom.citykey.domain.security.rootbeer.RootDetector
import com.telekom.citykey.domain.whats_new.WhatsNewInteractor
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.content.City
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.isFutureDay
import com.telekom.citykey.view.BaseViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.Calendar

class MainViewModel(
    private val globalData: GlobalData,
    private val preferencesHelper: PreferencesHelper,
    private val availableCitiesInteractor: AvailableCitiesInteractor,
    private val legalDataInteractor: LegalDataManager,
    inAppNotificationsInteractor: InAppNotificationsInteractor,
    private val identInteractor: IdentInteractor,
    private val rootDetector: RootDetector,
    private val oAuth2TokenManager: OAuth2TokenManager,
    private val oscaRepository: OscaRepository,
    private val whatsNewInteractor: WhatsNewInteractor
) : BaseViewModel() {

    val isFirstLaunch: LiveData<Boolean> get() = _isFirstLaunch
    val error: LiveData<Boolean> get() = _error
    val newCity: LiveData<City> get() = _newCity
    val promptLogin: LiveData<Unit> get() = _promptLogin

    val notifications get() = _notificationsMediator
    val enableNfc: LiveData<Boolean> get() = identInteractor.enableNfc
    val isCityActive: LiveData<Boolean> get() = _isCityActive
    val showDpnUpdates: LiveData<Unit> get() = _showDpnUpdates
    val unexpectedLogout: LiveData<LogoutReason> get() = globalData.unexpectedLogout
    val rootDetected: LiveData<Unit> get() = _rootDetected
    val forceUpdate: LiveData<Unit> get() = _forceUpdate

    private val _isCityActive: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _isFirstLaunch: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _error: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _newCity: MutableLiveData<City> = MutableLiveData()
    private val _notificationsHolder: MutableLiveData<Map<Int, Map<String, Int>>> = MutableLiveData()
    private val _notificationsMediator: MediatorLiveData<Map<Int, Map<String, Int>>> = MediatorLiveData()
    private val _showDpnUpdates: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _rootDetected: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _promptLogin: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _forceUpdate: SingleLiveEvent<Unit> = SingleLiveEvent()

    private var isSetupCompleted = false

    val shouldKeepSplashViewHidden: Boolean get() = isSetupCompleted

    init {
        _notificationsMediator.addSource(_newCity) {
            if (!_notificationsHolder.value.isNullOrEmpty()) _notificationsMediator.postValue(_notificationsHolder.value)
        }

        _notificationsMediator.addSource(
            inAppNotificationsInteractor.badgesCounter
                .toFlowable(BackpressureStrategy.LATEST)
                .toLiveData()
        ) {
            _notificationsHolder.value = it
            if (_newCity.value != null) _notificationsMediator.postValue(_notificationsHolder.value)
        }

        launch {
            globalData.city
                .distinctUntilChanged { val1, val2 -> val1.cityId == val2.cityId }
                .subscribe(_newCity::postValue)
        }
    }

    fun onActivityResumed() {
        setupViewModel()
    }

    private fun setupViewModel() {
        if (isSetupCompleted) {
            if (preferencesHelper.isFirstTime && !globalData.isUserLoggedIn) _isFirstLaunch.value = true
            else if (!globalData.hasUserAcceptedDpn) _showDpnUpdates.call()
            return
        }

        val isLoggedIn = oAuth2TokenManager.isLoggedIn
        val cityId =
            if (isLoggedIn && getDeepLinkCity() != 0) getDeepLinkCity() else preferencesHelper.getSelectedCityId()

        if (!isLoggedIn) showLoginScreenIfNeeded()

        launch {
            rootDetector.rootChecker
                .andThen(oscaRepository.checkAppVersion())
                .andThen(legalDataInteractor.loadLegalData())
                .andThen(globalData.loadUser(isLoggedIn))
                .andThen(availableCitiesInteractor.availableCities)
                .map { availableCities ->
                    if (cityId == -1) {
                        availableCities.first()
                    } else {
                        availableCities.firstOrNull { it.cityId == cityId } ?: availableCities.first()
                    }
                }
                .flatMap(globalData::loadCity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (preferencesHelper.isFirstTime && !globalData.isUserLoggedIn) _isFirstLaunch.value = true
                        else if (!globalData.hasUserAcceptedDpn) _showDpnUpdates.call()
                        isSetupCompleted = true
                    },
                    this::onError
                )
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.SERVICE_NOT_ACTIVE -> {
                            _isCityActive.postValue(false)
                            preferencesHelper.setSelectedCityId(-1)
                            setupViewModel()
                        }

                        else -> _error.postValue(true)
                    }
                }
            }

            is InvalidRefreshTokenException -> {
                globalData.logOutUser(throwable.reason)
            }

            is RootDetectedException -> _rootDetected.call()
            is UnsupportedVersionException -> _forceUpdate.value = Unit
            else -> _error.postValue(true)
        }
    }

    private fun showLoginScreenIfNeeded() {
        val logoutReason = preferencesHelper.logoutReason
        if (logoutReason == LogoutReason.TOKEN_EXPIRED_LOGOUT || logoutReason == LogoutReason.TECHNICAL_LOGOUT || logoutReason == LogoutReason.NO_LOGOUT_REASON)
            _promptLogin.value = Unit
    }

    fun onRetryClicked() {
        setupViewModel()
    }

    fun onNfcTagReceived(tag: Tag) {
        identInteractor.dispatchNfcTag(tag)
    }

    fun isPreviewMode(): Boolean = preferencesHelper.isPreviewMode

    fun setDeepLinkCity(eventCity: Int) {
        preferencesHelper.setDeepLinkCityId(eventCity)
    }

    fun getDeepLinkCity() = preferencesHelper.getDeepLinkCityId()

    fun setReloadCityData() {
        isSetupCompleted = false
    }

    fun isUserLoggedIn() = globalData.isUserLoggedIn

    fun shouldShowWhatsNewDialog(): Boolean = whatsNewInteractor.shouldShowWhatsNew()

    fun whatsNewShown() = whatsNewInteractor.whatsNewShown()

    fun onActivityStopped(appTask: ActivityManager.AppTask, activityClassName: String) {
        if (activityClassName.contentEquals(appTask.taskInfo.topActivity?.className)) {
            preferencesHelper.saveAppBackgroundingTimestamp(Calendar.getInstance().timeInMillis)
        }
    }

    fun needsResettingEventTracking(): Boolean {
        val appBackgroundingTimestamp = preferencesHelper.getAppBackgroundingTimestamp()
        if (appBackgroundingTimestamp > 0) {
            return Calendar.getInstance().apply { timeInMillis = appBackgroundingTimestamp }.isFutureDay()
        }
        return false
    }

}
