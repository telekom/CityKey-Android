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

package com.telekom.citykey.domain.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.adjust.sdk.Adjust
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.TpnsRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.api.requests.TpnsParam
import com.telekom.citykey.models.api.requests.TpnsRegisterRequest
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class TpnsManager(
    private val context: Context,
    private val globalData: GlobalData,
    private val sharedPreferences: SharedPreferences,
    private val repository: TpnsRepository,
) {
    companion object {
        var pushId: String = ""
            private set

        private const val PREF_DEVICE_ID = "PREF_DEVICE_ID"
        private const val PREF_TPNS_REGISTERED = "PREF_TPNS_REGISTERED"

        private const val PARAM_ACTIVE_USER = "USER_ACTIVE"
        private const val PARAM_CITY_ID = "CITY_ID"
        private const val PARAM_PUSH_USER_ID = "USER_ID"
    }

    private var homeCityId: String = "-1"
    private var pushUserId: String = "-1"

    private val deviceId: String? get() = sharedPreferences.getString(PREF_DEVICE_ID, null)
    private val isRegistered: Boolean get() = sharedPreferences.getBoolean(PREF_TPNS_REGISTERED, false)
    private val randomId: String get() = UUID.randomUUID().toString() + System.currentTimeMillis()
    private var deviceRegistrationId: String = "-1"

    private var registrationDisposable: Disposable? = null

    @SuppressLint("CheckResult")
    private fun observeUser() {
        globalData.user
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged { val1, val2 -> val1 == val2 }
            .filter { it is UserState.Present }
            .map { (it as UserState.Present).profile }
            .subscribe { profile ->
                homeCityId = profile.homeCityId.toString()
                pushUserId = profile.accountId
                registerTpns(true)
            }
    }

    fun initPushNotifications() {
        FirebaseApp.initializeApp(context)
        if (deviceId.isNullOrEmpty()) setDeviceId()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                deviceRegistrationId = task.result.toString()
                deviceId?.let { pushId = it }
                Adjust.setPushToken(deviceRegistrationId, context)

                if (!isRegistered) registerTpns(false)

                observeUser()
            }
        )
    }

    private fun registerTpns(isActive: Boolean) {
        val additionalParams = listOf(
            TpnsParam(PARAM_ACTIVE_USER, isActive.toString()),
            TpnsParam(PARAM_CITY_ID, homeCityId),
            TpnsParam(PARAM_PUSH_USER_ID, pushUserId)
        )

        val tpnsRegisterRequest = TpnsRegisterRequest(
            applicationKey = BuildConfig.APPLICATION_ID,
            deviceId = deviceId ?: "",
            deviceRegistrationId = deviceRegistrationId,
            additionalParameters = additionalParams
        )

        registrationDisposable?.dispose()
        registrationDisposable = repository.registerForTpns(tpnsRegisterRequest)
            .retryWhen { errors ->
                errors.flatMap { error ->
                    if (error is HttpException && error.code() / 100 == 5 || error is NoConnectionException)
                        Flowable.just("Tpns_Retry")
                    else Flowable.error(error)
                }
            }
            .subscribe({ setDeviceRegistered() }, Timber::e)
    }

    private fun setDeviceRegistered() {
        sharedPreferences.edit { putBoolean(PREF_TPNS_REGISTERED, true) }
    }

    private fun setDeviceId() {
        sharedPreferences.edit { putString(PREF_DEVICE_ID, randomId) }
    }
}
