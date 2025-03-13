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

package com.telekom.citykey.view.user.login.login

import android.annotation.SuppressLint
import android.app.PendingIntent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.ResolvableApiException
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.domain.user.smartlock.CredentialsClientHandler
import com.telekom.citykey.domain.user.smartlock.ResolvableException
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.LogInRequest
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LoginViewModel(
    private val globalData: GlobalData,
    private val userInteractor: UserInteractor,
    private val availableCitiesInteractor: AvailableCitiesInteractor,
    private val smartLockCredentials: CredentialsClientHandler,
    private val preferencesHelper: PreferencesHelper
) : NetworkingViewModel() {

    val login: LiveData<Boolean> get() = _login
    val userProfile: LiveData<UserProfile> get() = _userProfile
    val loginHint: LiveData<LogoutReason> get() = _loginHint
    val error: LiveData<FieldValidation> get() = _error
    val emailNotConfirmed: LiveData<String?> get() = _emailNotConfirmed
    val credentials: LiveData<Credential> get() = _credentials
    val resolutions: LiveData<PendingIntent> get() = _resolutions
    val resolutionSave: LiveData<PendingIntent> get() = _resolutionsSave
    val showDpnUpdates: LiveData<Unit> get() = _showDpnUpdates

    private val _login: MutableLiveData<Boolean> = MutableLiveData()
    private val _userProfile: MutableLiveData<UserProfile> = MutableLiveData()
    private val _loginHint: MutableLiveData<LogoutReason> = MutableLiveData()
    private val _error: MutableLiveData<FieldValidation> = MutableLiveData()
    private val _emailNotConfirmed: SingleLiveEvent<String?> = SingleLiveEvent()
    private val _credentials: SingleLiveEvent<Credential> = SingleLiveEvent()
    private val _resolutions: SingleLiveEvent<PendingIntent> = SingleLiveEvent()
    private val _resolutionsSave: SingleLiveEvent<PendingIntent> = SingleLiveEvent()
    private val _showDpnUpdates: SingleLiveEvent<Unit> = SingleLiveEvent()

    private var email: String = ""

    init {
        setupSmartLockCredentials()
        _loginHint.postValue(preferencesHelper.logoutReason)
        preferencesHelper.setLogoutReason(LogoutReason.INVALID)
    }

    private fun setupSmartLockCredentials() {
        launch {
            smartLockCredentials.retrieveCredentials()
                .timeout(10L, TimeUnit.SECONDS, Observable.empty())
                .subscribe(_credentials::postValue) {
                    val throwable = it as ResolvableException
                    if (throwable.exception is ResolvableApiException) {
                        _resolutions.postValue(throwable.exception.resolution)
                    } else {
                        Timber.e(throwable.exception)
                    }
                }
        }
    }

    fun onLoginBtnPressed(email: String, password: String, stayLoggedIn: Boolean, isFirstTime: Boolean) {
        this.email = email
        launch {
            userInteractor.logUserIn(LogInRequest(email.lowercase(), password), stayLoggedIn)
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .doOnSuccess { preferencesHelper.saveKeepMeLoggedIn(stayLoggedIn) }
                .subscribe(
                    {
                        observeUser()
                        if (isFirstTime && it.homeCityId != 0) tryLoadingUserHomeCity(password, it)
                        else saveOnSmartLock(password, it)
                    },
                    this::onError
                )
        }
    }

    private fun tryLoadingUserHomeCity(password: String, userProfile: UserProfile) {
        launch {
            availableCitiesInteractor.availableCities
                .map { availableCities ->
                    availableCities.firstOrNull { it.postalCode?.contains(userProfile.postalCode) == true }
                        ?: availableCities.first()
                }
                .flatMap(globalData::loadCity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { saveOnSmartLock(password, userProfile) }
        }
    }

    private fun saveOnSmartLock(password: String, user: UserProfile) {
        preferencesHelper.setFirstTimeFinished()
        launch {
            smartLockCredentials.saveCredentials(user.email, password)
                .subscribe(
                    {
                        if (!user.dpnAccepted) _showDpnUpdates.call()
                        else _login.postValue(true)
                    },
                    {
                        when (it) {
                            is ResolvableException -> {
                                if (it.exception is ResolvableApiException) {
                                    _resolutionsSave.postValue(it.exception.resolution)
                                } else {
                                    if (!user.dpnAccepted) _showDpnUpdates.call()
                                    else _login.postValue(true)
                                    Timber.e(it.exception)
                                }
                            }

                            else -> {
                                if (!user.dpnAccepted) _showDpnUpdates.call()
                                else _login.postValue(true)
                                Timber.e(it)
                            }
                        }
                    }
                )
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.LOGIN_EMAIL_NOT_CONFIRMED ->
                            _emailNotConfirmed.postValue(null)

                        ErrorCodes.EMAIL_RESEND_SOON -> _emailNotConfirmed.postValue(it.userMsg)
                        else -> _error.postValue(FieldValidation(FieldValidation.ERROR, it.userMsg))
                    }
                }
            }

            is NoConnectionException -> showRetry()
            else -> _technicalError.postValue(Unit)
        }
    }

    @SuppressLint("CheckResult")
    private fun observeUser() {
        globalData.user
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged { val1, val2 -> val1 == val2 }
            .filter { it is UserState.Present }
            .map { (it as UserState.Present).profile }
            .subscribe { profile ->
                _userProfile.postValue(profile)
                preferencesHelper.setUserPostalCode(profile.postalCode)
            }
    }

}
