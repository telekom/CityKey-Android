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
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.TpnsRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.networkinterface.models.api.requests.TpnsParam
import com.telekom.citykey.networkinterface.models.api.requests.TpnsRegisterRequest
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TpnsManager(
    private val context: Context,
    private val globalData: GlobalData,
    private val sharedPreferences: SharedPreferences,
    private val repository: TpnsRepository,
) {

    private val mScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {

        var pushId: String = ""
            private set

        private const val PREF_DEVICE_ID = "PREF_DEVICE_ID"

        @Deprecated(
            message = "Used earlier to prevent multiple first-time TPNS requests, type Boolean",
            level = DeprecationLevel.ERROR
        )
        private const val PREF_TPNS_REGISTERED = "PREF_TPNS_REGISTERED"

        private const val PARAM_ACTIVE_USER = "USER_ACTIVE"
        private const val PARAM_CITY_ID = "CITY_ID"
        private const val PARAM_PUSH_USER_ID = "USER_ID"
    }

    private val deviceId: String? get() = sharedPreferences.getString(PREF_DEVICE_ID, null)

    private var deviceRegistrationId: String = "-1"

    private var registrationDisposable: Disposable? = null

    /**
     * Initializes push notifications by setting up Firebase, managing the device ID and FCM token,
     * and observing user changes to handle TPNS registration.
     */
    fun initPushNotifications() = mScope.launch {

        // Initiate Firebase in all contexts
        FirebaseApp.initializeApp(context)

        // Handle the DeviceID
        manageDeviceID()

        // Handle the FCM token
        manageFCMToken()

        // Observe Forever for User changes
        observeCityAndUserChanges()
    }

    fun onFCMTokenChanged() = mScope.launch { manageFCMToken() }

    /**
     * Manages the Device ID by generating a new one if it does not exist and storing it in shared preferences.
     * If a device ID already exists, it sets the `pushId` to the existing device ID.
     */
    private fun manageDeviceID() {

        if (deviceId.isNullOrEmpty()) {

            // Probably the first time app is launched..
            val generatedRandomDeviceID = UUID.randomUUID().toString() + System.currentTimeMillis()

            // Commit it directly to disk right now!
            sharedPreferences.edit(commit = true) {
                putString(PREF_DEVICE_ID, generatedRandomDeviceID)
            }
        }

        deviceId?.let { pushId = it }
    }

    /**
     * Manages the Firebase Cloud Messaging (FCM) token by retrieving it from Firebase and setting it in the Adjust SDK.
     * If the device is not registered, it triggers the TPNS registration process.
     */
    private suspend fun manageFCMToken() {

        // Get the Token from Firebase, or return on error
        val fcmToken: String = getFCMToken() ?: return

        deviceRegistrationId = fcmToken
        Adjust.setPushToken(fcmToken, context)
    }

    /**
     * Retrieves the Firebase Cloud Messaging (FCM) token.
     *
     * @return The FCM token if successful, null otherwise.
     */
    private suspend fun getFCMToken(): String? = suspendCoroutine { cont ->
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                cont.resume(task.takeIf { it.isSuccessful }?.result)
            }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }

    /**
     * Observes changes in the city and user data to handle TPNS registration.
     *
     * This function sets up observers to monitor changes in the user's city and login state.
     * It registers the device for TPNS when the city changes for users who are not logged in,
     * and updates the TPNS registration when the user logs in.
     */
    @SuppressLint("CheckResult")
    private fun observeCityAndUserChanges() {

        /**
         * We want to target such users who have not yet logged into Citykey, but are using it to view data of
         * a particular city. So we'll update such users to TPNS whenever city changes!
         */
        Observable.combineLatest(
            globalData.user,
            globalData.city
        ) { user, city -> user to city }
            .filter { it.first is UserState.Absent }
            .subscribeOn(Schedulers.io())
            .subscribe(
                { userToCity ->
                    Timber.tag("TPNS").d("Register Absent user as city changed to ${userToCity.second.cityName}")
                    registerTpns(
                        isActive = false,
                        cityId = userToCity.second.cityId.toString(),
                        userId = "-1"
                    )
                },
                Timber::e
            )

        /**
         * As soon as user logs into the application, the subscriber above won't work anymore! This is then time
         * to update TPNS that this user has finally logged into Citykey and that it is to be subscribed to only
         * one particular city!
         */
        globalData.user
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged { val1, val2 -> val1 == val2 }
            .filter { it is UserState.Present }
            .map { (it as UserState.Present).profile }
            .subscribe(
                { profile ->
                    val homeCityId = profile.homeCityId.toString()
                    val pushUserId = profile.accountId
                    Timber.tag("TPNS").d("Register Present user as home city is ${profile.cityName}")
                    registerTpns(
                        isActive = true,
                        cityId = homeCityId,
                        userId = pushUserId
                    )
                },
                Timber::e
            )
    }

    /**
     * Registers the device for TPNS (Telekom Push Notification Service) with the given parameters.
     *
     * @param isActive Indicates whether the user is active.
     * @param cityId The ID of the city to register the device for. Defaults to the home city ID.
     */
    private fun registerTpns(isActive: Boolean, cityId: String, userId: String) {

        // TPNS uses these parameters to select the users to send mass notifications to!
        val additionalParams = listOf(
            TpnsParam(PARAM_ACTIVE_USER, isActive.toString()),
            TpnsParam(PARAM_CITY_ID, cityId),
            TpnsParam(PARAM_PUSH_USER_ID, userId)
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
                    if (error is HttpException && error.code() / 100 == 5 || error is NoConnectionException) {
                        Flowable.just("Tpns_Retry")
                    } else {
                        Flowable.error(error)
                    }
                }
            }
            .subscribe({}, Timber::e)
    }
}
