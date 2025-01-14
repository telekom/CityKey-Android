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

package com.telekom.citykey.domain.track

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.moengage.core.DataCenter
import com.moengage.core.MoEngage
import com.moengage.core.analytics.MoEAnalyticsHelper
import com.moengage.core.config.LogConfig
import com.moengage.core.disableDataTracking
import com.moengage.core.enableDataTracking
import com.moengage.core.model.AppStatus
import com.moengage.inapp.MoEInAppHelper
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.domain.track.callback.ClickActionCallback
import com.telekom.citykey.domain.track.callback.InAppLifecycleCallbacks
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.extensions.addPartnerAndCallbackParameter
import com.telekom.citykey.utils.extensions.isAppInstall
import com.telekom.citykey.utils.extensions.isQaBuild
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * This class is used for tracking some user specific information like events, sessions, installs,
 * uninstalls and others
 */
class AdjustManager(private val context: Context, private val preferencesHelper: PreferencesHelper) {

    val isAnalyticsEventTrackingAllowed: Boolean get() = preferencesHelper.getAdjustTrackingAllowed()

    private val userProfile: UserProfile? get() = preferencesHelper.getUserProfile()
    private val currentCityId: Int get() = preferencesHelper.getSelectedCityId()
    private val currentCityName: String? get() = preferencesHelper.getSelectedCityName()
    private val userPostalCode: String? get() = preferencesHelper.getUserPostalCode()

    private var moEngageManager: MoEngageManager? = null

    private var isAdjustAdIdConfigured = false
    private val adjustEventsToBeTracked = mutableListOf<AdjustEvent>()

    private val oneTimeEventTrackedMap = mutableMapOf(
        Pair(R.string.app_launched, false),
        Pair(R.string.open_home, false),
        Pair(R.string.open_service, false),
        Pair(R.string.open_infobox, false)
    )

    private var cachedAdId: String? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val environment = AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(context, BuildConfig.ADJUST_APP_TOKEN, environment)
        config.enableSendingInBackground()
        config.setLogLevel(LogLevel.VERBOSE)
        config.setOnAttributionChangedListener {
            updateAnalyticsSdkConfigs()
            updateMoEngageUserAttributes()
        }
        Adjust.initSdk(config)

    }

    fun initialiseMoEngage(application: Application) {
        if (moEngageManager == null) {
            moEngageManager = MoEngageManager(application, preferencesHelper)
            updateAnalyticsSdkConfigs()
            updateMoEngageUserAttributes()
        }
    }


    // NOTE: The function for tracking `AppLaunched` event is separate, because:
    // - It is always tracked for `QA` build and respects the data privacy settings for `Production` build
    // - For FTU interaction it is tracked after user acted confirmed analytics settings, and at other times when
    // `AdjustManager` is initialised
    fun trackAppLaunchedEvent() {
        if (isQaBuild || isAnalyticsEventTrackingAllowed) {
            if (preferencesHelper.isTrackingConfirmed && oneTimeEventTrackedMap[R.string.app_launched] != true) {
                trackEvent(R.string.app_launched)
                oneTimeEventTrackedMap[R.string.app_launched] = true
            }
        }
    }

    fun trackOneTimeEvent(@StringRes eventToken: Int) {
        if (oneTimeEventTrackedMap[eventToken] != true) {
            if (eventToken == R.string.open_home) trackAppLaunchedEvent()
            trackEvent(eventToken)
            oneTimeEventTrackedMap[eventToken] = true
        }
    }

    fun resetOneTimeEventsTracker() {
        oneTimeEventTrackedMap.keys.forEach { oneTimeEventTrackedMap[it] = false }
    }

    fun trackEvent(@StringRes eventResId: Int, paramsMap: Map<String, String> = mapOf()) = coroutineScope.launch {
        val eventToken = context.getString(eventResId)
        val event = AdjustEvent(eventToken)
        configureEventParams(event)
        for ((key, value) in paramsMap) {
            event.addPartnerAndCallbackParameter(key, value)
        }
        if (isAdjustAdIdConfigured) {
            Adjust.trackEvent(event)
            MoEInAppHelper.getInstance().setInAppContext(setOf(eventToken))
            MoEInAppHelper.getInstance().showInApp(context)
        } else {
            adjustEventsToBeTracked.add(event)
        }
    }

    fun updateTrackingPermissions(allowed: Boolean) {
        preferencesHelper.setAdjustTrackingAllowed(allowed)

        updateAnalyticsSdkConfigs()
        updateMoEngageUserAttributes()
    }

    private suspend fun configureEventParams(event: AdjustEvent) {
        // Add Partner Parameters
        val adjustAdId: String? = getAdjustAdId()
        event.addPartnerParameter(AnalyticsParameterKey.adjustDeviceId, adjustAdId)
        event.addPartnerParameter(AnalyticsParameterKey.moengageCustomerId, adjustAdId)

        // Add Partner & Callback Parameters
        event.addPartnerAndCallbackParameter(
            AnalyticsParameterKey.userStatus,
            if (userPostalCode.isNullOrBlank())
                AnalyticsParameterValue.notLoggedIn
            else
                AnalyticsParameterValue.loggedIn
        )

        event.addPartnerAndCallbackParameter(
            AnalyticsParameterKey.userZipCode,
            if (userPostalCode.isNullOrBlank()) AnalyticsParameterValue.empty else userPostalCode.toString()
        )

        event.addPartnerAndCallbackParameter(
            AnalyticsParameterKey.registeredCityId, userProfile?.homeCityId?.toString() ?: AnalyticsParameterValue.empty
        )
        event.addPartnerAndCallbackParameter(
            AnalyticsParameterKey.registeredCityName,
            userProfile?.cityName ?: AnalyticsParameterValue.empty
        )

        if (currentCityId > 0) {
            event.addPartnerAndCallbackParameter(AnalyticsParameterKey.selectedCityId, currentCityId.toString())
            event.addPartnerAndCallbackParameter(
                AnalyticsParameterKey.selectedCityName,
                currentCityName ?: AnalyticsParameterValue.empty
            )
        }
    }

    private fun updateAnalyticsSdkConfigs() {
        if (isAnalyticsEventTrackingAllowed) {
            Adjust.enable()
        }
        moEngageManager?.let {
            try {
                if (isAnalyticsEventTrackingAllowed) enableDataTracking(context) else disableDataTracking(context)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun updateMoEngageUserAttributes() = coroutineScope.launch {
        val adjustAdId: String? = getAdjustAdId()
        moEngageManager?.setupMoEngageUserAttributes(adjustAdId, userProfile)
        if (adjustAdId.isNullOrBlank().not() && isAdjustAdIdConfigured.not()) {
            adjustEventsToBeTracked.forEach {
                it.addPartnerParameter(AnalyticsParameterKey.adjustDeviceId, adjustAdId)
                it.addPartnerParameter(AnalyticsParameterKey.moengageCustomerId, adjustAdId)
                Adjust.trackEvent(it)
            }
            adjustEventsToBeTracked.clear()
            isAdjustAdIdConfigured = true
        }
    }

    private suspend fun getAdjustAdId(): String? = suspendCancellableCoroutine { cont ->
        if (cachedAdId != null) {
            cont.resume(cachedAdId)
        } else {
            Adjust.getAdid { id ->
                cachedAdId = id
                cont.resume(id)
            }
        }
    }
}

private class MoEngageManager(private val application: Application, private val preferencesHelper: PreferencesHelper) {

    init {
        val moEngage = MoEngage.Builder(application, BuildConfig.MOENGAGE_APP_ID, DataCenter.DATA_CENTER_2)
            .configureLogs(LogConfig(com.moengage.core.LogLevel.NO_LOG))
            .build()

        MoEngage.initialiseDefaultInstance(moEngage)
        MoEAnalyticsHelper.trackDeviceLocale(application)

        if (application.isAppInstall()) {
            MoEAnalyticsHelper.setAppStatus(application, AppStatus.INSTALL)
        } else {
            checkForAppUpdateStatus()
        }

        setupInAppCallbacks()
    }

    private fun checkForAppUpdateStatus() {
        val preferencesAppVersion = preferencesHelper.getSavedAppVersion()
        if (preferencesHelper.isFirstTime.not() && preferencesAppVersion != BuildConfig.APP_VERSION) {
            MoEAnalyticsHelper.setAppStatus(application, AppStatus.UPDATE)
            preferencesHelper.updateAppVersion(BuildConfig.APP_VERSION)
        }
    }

    private fun setupInAppCallbacks() {
        MoEInAppHelper.getInstance().addInAppLifeCycleListener(InAppLifecycleCallbacks())
        // Callback for in-app campaign's ActionListener
        MoEInAppHelper.getInstance().setClickActionListener(ClickActionCallback())
    }

    fun setupMoEngageUserAttributes(adjustAdId: String?, userProfile: UserProfile?) {
        adjustAdId?.let {
            MoEAnalyticsHelper.setUniqueId(application, adjustAdId)
            MoEAnalyticsHelper.setUserAttribute(application, AnalyticsParameterKey.moengageCustomerId, adjustAdId)
        }

        var registeredCityId = ""
        var registeredCityName = ""
        if (preferencesHelper.getAdjustTrackingAllowed()) {
            userProfile?.let {
                registeredCityId = it.homeCityId.toString()
                registeredCityName = it.cityName
            }
        }
        MoEAnalyticsHelper.setUserAttribute(application, AnalyticsParameterKey.registeredCityId, registeredCityId)
        MoEAnalyticsHelper.setUserAttribute(application, AnalyticsParameterKey.registeredCityName, registeredCityName)
    }

}
