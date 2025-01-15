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

package com.telekom.citykey.utils.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import java.util.*

fun Context.hasPermission(perm: String) =
    ActivityCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED

inline fun <reified T : AppWidgetProvider> Context.removeMidnightUpdateForWidget(action: String) {
    val intent = Intent(this, T::class.java).setAction(action)
    val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}

inline fun <reified T : AppWidgetProvider> Context.scheduleMidnightUpdateForWidget(action: String) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, T::class.java).setAction(action)
    val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val midnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 10)
        add(Calendar.DAY_OF_YEAR, 1)
    }
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, midnight.timeInMillis, 86400000, pendingIntent)
}

fun Context.isAppInstall(): Boolean =
    try {
        val packageInfo = getPackageInfoCompat()
        packageInfo.firstInstallTime == packageInfo.lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        true
    }

fun Context.getPackageInfoCompat(): PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
} else {
    @Suppress("DEPRECATION")
    packageManager.getPackageInfo(packageName, 0)
}
