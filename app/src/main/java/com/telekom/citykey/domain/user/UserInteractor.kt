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

package com.telekom.citykey.domain.user

import androidx.lifecycle.LiveData
import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.api.requests.LogInRequest
import com.telekom.citykey.models.api.requests.LogOutRequest
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.user.login.LogoutReason
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class UserInteractor(
    private val userRepository: UserRepository,
    private val preferencesHelper: PreferencesHelper,
    private val oAuth2TokenManager: OAuth2TokenManager
): KoinComponent {
    private var userLogoutDisposable: Disposable? = null
    private val _user: BehaviorSubject<UserState> = BehaviorSubject.create()
    private val _unexpectedLogout: SingleLiveEvent<LogoutReason> = SingleLiveEvent()
    val user: Observable<UserState> get() = _user.hide()
    val unexpectedLogout: LiveData<LogoutReason> get() = _unexpectedLogout

    val isUserLoggedIn get() = _user.value is UserState.Present
    val userCityName get() = (_user.value as? UserState.Present)?.profile?.cityName
    val userCityId get() = (_user.value as? UserState.Present)?.profile?.homeCityId
    val userId get() = (_user.value as? UserState.Present)?.profile?.accountId
    val hasAcceptedDpn get() = (_user.value as? UserState.Present)?.profile?.dpnAccepted ?: true

    val selectedCityId get() = preferencesHelper.getSelectedCityId()

    private val adjustManager: AdjustManager by inject()

    fun updatePersonalDataLocally(newPostalCode: String, newCityName: String, newCityId: Int) {
        (_user.value as? UserState.Present)?.profile?.apply {
            postalCode = newPostalCode
            cityName = newCityName
            homeCityId = newCityId
        }?.also {
            preferencesHelper.saveUserProfile(it)
            adjustManager.updateMoEngageUserAttributes()
            _user.onNext(UserState.Present(it))
        }
    }

    fun updateUser(): Maybe<UserProfile> = userRepository.getProfile()
        .doOnError(Timber::e)
        .doOnSuccess { oAuth2TokenManager.updateUserId(it.accountId) }
        .doOnSuccess {
            preferencesHelper.saveUserProfile(it)
            adjustManager.updateMoEngageUserAttributes()
            _user.onNext(UserState.Present(it))
        }

    fun logUserIn(request: LogInRequest, stayLoggedIn: Boolean) = userRepository.login(request, stayLoggedIn)
        .doOnSuccess { oAuth2TokenManager.updateCredentials(it, stayLoggedIn) }
        .flatMap { updateUser() }
        .doOnError { logOutUser(LogoutReason.NO_LOGOUT_REASON) }

    fun logOutUser(logoutReason: LogoutReason = LogoutReason.TECHNICAL_LOGOUT) {
        if (logoutReason == LogoutReason.TECHNICAL_LOGOUT || logoutReason == LogoutReason.TOKEN_EXPIRED_LOGOUT) {
            _unexpectedLogout.postValue(logoutReason)
        }
        if (isUserLoggedIn) _user.onNext(UserState.Absent)
        preferencesHelper.setLogoutReason(logoutReason)
        preferencesHelper.togglePreviewMode(false)
        preferencesHelper.setUserPostalCode("")
        preferencesHelper.clearUserProfile()
        adjustManager.updateMoEngageUserAttributes()
        notifyBackendAboutLogout()
    }

    private fun notifyBackendAboutLogout() {
        oAuth2TokenManager.refreshToken?.let {
            val token = it
            oAuth2TokenManager.logOut()
            if (token.isNotBlank()) {
                userLogoutDisposable?.dispose()
                userLogoutDisposable = userRepository.logout(LogOutRequest(token))
            }
        }
    }

    fun setUserAbsent() {
        _user.onNext(UserState.Absent)
    }

    fun isPreviewMode(): Boolean = preferencesHelper.isPreviewMode

    fun togglePreviewMode(shouldEnable: Boolean) = preferencesHelper.togglePreviewMode(shouldEnable)
}
