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

package com.telekom.citykey.domain.repository

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.domain.auth.OAuth2TokenManager
import com.telekom.citykey.networkinterface.client.CitykeyAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyAuthAPIClient
import com.telekom.citykey.networkinterface.models.OscaResponse
import com.telekom.citykey.networkinterface.models.api.requests.DeleteAccountRequest
import com.telekom.citykey.networkinterface.models.api.requests.EmailChangeRequest
import com.telekom.citykey.networkinterface.models.api.requests.LogInRequest
import com.telekom.citykey.networkinterface.models.api.requests.LogOutRequest
import com.telekom.citykey.networkinterface.models.api.requests.NewPasswordRequest
import com.telekom.citykey.networkinterface.models.api.requests.PasswordChangeRequest
import com.telekom.citykey.networkinterface.models.api.requests.PersonalDetailChangeRequest
import com.telekom.citykey.networkinterface.models.api.requests.PinConfirmationRequest
import com.telekom.citykey.networkinterface.models.api.requests.RegistrationRequest
import com.telekom.citykey.networkinterface.models.api.requests.ResendPinRequest
import com.telekom.citykey.networkinterface.models.content.RegistrationResponse
import com.telekom.citykey.networkinterface.models.content.UserProfile
import com.telekom.citykey.networkinterface.models.user.Credentials
import com.telekom.citykey.networkinterface.models.user.ResidenceValidationResponse
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class UserRepository(
    private val api: CitykeyAPIClient,
    private val authApi: CitykeyAuthAPIClient,
) {

    fun login(request: LogInRequest, keepLoggedIn: Boolean): Maybe<Credentials> = api.login(
        request = request,
        keepLoggedIn = keepLoggedIn,
        cityId = BuildConfig.CITY_ID
    ).subscribeOn(Schedulers.io()).map { it.content }

    fun logout(request: LogOutRequest): Disposable = api.logout(
        request = request,
        cityId = BuildConfig.CITY_ID,
        keepLoggedIn = OAuth2TokenManager.keepMeLoggedIn
    ).subscribeOn(Schedulers.io())
        .onErrorComplete()
        .subscribe()

    fun register(request: RegistrationRequest): Maybe<OscaResponse<RegistrationResponse>> =
        api.register(request, BuildConfig.CITY_ID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun resendPIN(
        request: ResendPinRequest,
        actionName: String
    ): Completable = api.resendPINEmail(request, cityId = BuildConfig.CITY_ID, actionName = actionName)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun deleteUser(request: DeleteAccountRequest): Completable = authApi.deleteUser(request, BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getProfile(): Maybe<UserProfile> = authApi.getUserProfile(BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .map { it.content }
        .observeOn(AndroidSchedulers.mainThread())

    fun confirmRegistration(request: PinConfirmationRequest, actionName: String): Completable =
        api.setRegistrationConfirmation(request, cityId = BuildConfig.CITY_ID, actionName = actionName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun resetPassword(request: NewPasswordRequest): Completable = api.requestNewPassword(request, BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun changePassword(request: PasswordChangeRequest): Completable = authApi.changePassword(request, BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun validatePostalCode(postalCode: String): Maybe<ResidenceValidationResponse> = authApi.validatePostalCode(postalCode, BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .map { it.content }
        .observeOn(AndroidSchedulers.mainThread())

    fun changePersonalData(request: PersonalDetailChangeRequest, update: String): Completable =
        authApi.changePersonalData(request, update, BuildConfig.CITY_ID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun changeEmail(request: EmailChangeRequest): Completable = authApi.changeEmail(request, BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
