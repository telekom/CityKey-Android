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

package com.telekom.citykey

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.Process
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.distribute.Distribute
import com.telekom.citykey.common.FileLoggingTree
import com.telekom.citykey.di.citykeyKoinModules
import com.telekom.citykey.domain.notifications.TpnsManager
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.network.di.networkModule
import com.telekom.citykey.utils.PreferencesHelper
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SmartApplication : Application() {

    companion object {
        private const val AA2_PROCESS = "ausweisapp2_service"
    }

    private val adjustManager: AdjustManager by inject()
    private val tpnsManager: TpnsManager by inject()
    private val preferencesHelper: PreferencesHelper by inject()

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

        when {
            BuildConfig.FLAVOR != "production" -> Timber.plant(FileLoggingTree(this))
            BuildConfig.DEBUG -> Timber.plant(Timber.DebugTree())
        }

        if (isAA2Process()) {
            Timber.i("Application is instantiated again by AA2 Process")
            return
        }

        startKoin {
            androidContext(this@SmartApplication)
            modules(citykeyKoinModules + networkModule)
        }

//        if (BuildConfig.APPCENTER_ID.isNotEmpty()) {
//            AppCenter.start(this, BuildConfig.APPCENTER_ID, Distribute::class.java)
//        }

        tpnsManager.initPushNotifications()
        adjustManager.initialiseMoEngage(this)
        preferencesHelper.togglePreviewMode(false)

    }

    private fun isAA2Process(): Boolean {
        if (Build.VERSION.SDK_INT >= 28) {
            return getProcessName().endsWith(AA2_PROCESS)
        }
        val pid = Process.myPid()
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in manager.runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName.endsWith(AA2_PROCESS)
            }
        }
        return false
    }
}
