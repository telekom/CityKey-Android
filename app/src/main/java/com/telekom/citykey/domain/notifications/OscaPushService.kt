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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.utils.ForegroundUtil
import com.telekom.citykey.view.main.MainActivity
import org.koin.android.ext.android.inject

class OscaPushService : FirebaseMessagingService() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "OSCA_NOTIFICATION_CHANNEL"

        private const val DATA_PARAM_TITLE = "title"
        private const val DATA_PARAM_MESSAGE = "message"
        private const val DATA_PARAM_PRIORITY = "oscaPriority"
        private const val DATA_PARAM_DEEPLINK = "deeplink"

        private const val PRIORITY_PARAM = "oscaPriority"
        const val NOTIFICATION_PRIORITY_HIGH = "High"
        const val NOTIFICATION_PRIORITY_MEDIUM = "Medium"
        const val NOTIFICATION_PRIORITY_LOW = "Low"

        const val DEEPLINK_PARAM = "deeplink"
        const val CITYKEY_DEEP_LINK_URI_IDENTIFIER = "citykey://"
        const val EVENT_DEEP_LINK_URI_IDENTIFIER = "events"
        const val INFOBOX_DEEP_LINK_URI_IDENTIFIER = "infobox"
    }

    private val globalData: GlobalData by inject()

    private val isAppRunning get() = ForegroundUtil.isApRunning(applicationContext)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            processNotificationPushMessage(remoteMessage)
        } ?: kotlin.run {
            processDataPushMessage(remoteMessage)
        }
    }

    private fun processDataPushMessage(remoteMessage: RemoteMessage) {
        var notificationTitle = remoteMessage.data[DATA_PARAM_TITLE]
        val notificationMessage = remoteMessage.data[DATA_PARAM_MESSAGE]

        if (notificationMessage.isNullOrBlank() && notificationTitle.isNullOrBlank()) {
            return
        }

        if (notificationMessage.isNullOrBlank().not() && notificationTitle.isNullOrBlank()) {
            notificationTitle = getString(R.string.app_name)
        }

        val notificationPriority = remoteMessage.data[DATA_PARAM_PRIORITY] ?: NOTIFICATION_PRIORITY_HIGH
        val notificationDeeplink = remoteMessage.data[DATA_PARAM_DEEPLINK].orEmpty()

        val notificationHasLowPriorityEventDeeplink =
            notificationPriority.equals(NOTIFICATION_PRIORITY_LOW, ignoreCase = true) &&
                    notificationDeeplink.contains(INFOBOX_DEEP_LINK_URI_IDENTIFIER)
        if (isAppRunning && notificationHasLowPriorityEventDeeplink) {
            globalData.refreshContent().subscribe()
        }
        showNotification(
            notificationTitle = notificationTitle,
            notificationMessage = notificationMessage,
            deepLinkUrl = notificationDeeplink,
            notificationPriority = notificationPriority
        )
    }

    private fun processNotificationPushMessage(remoteMessage: RemoteMessage) {
        var notificationTitle = remoteMessage.notification?.title
        val notificationMessage = remoteMessage.notification?.body
        if (notificationMessage.isNullOrBlank() && notificationTitle.isNullOrBlank()) {
            return
        }

        if (notificationMessage.isNullOrBlank().not() && notificationTitle.isNullOrBlank()) {
            notificationTitle = getString(R.string.app_name)
        }

        val notificationPriority = remoteMessage.data[PRIORITY_PARAM] ?: NOTIFICATION_PRIORITY_HIGH
        if (notificationPriority.equals(NOTIFICATION_PRIORITY_LOW, ignoreCase = true)) {
            globalData.refreshContent().subscribe()
        }
        showNotification(
            notificationTitle = notificationTitle,
            notificationMessage = notificationMessage,
            deepLinkUrl = remoteMessage.data[DEEPLINK_PARAM].orEmpty(),
            notificationPriority = notificationPriority
        )
    }

    private fun showNotification(
        notificationTitle: String?,
        notificationMessage: String?,
        deepLinkUrl: String,
        notificationPriority: String
    ) {
        val importance = when (notificationPriority.lowercase()) {
            "low" -> NotificationManagerCompat.IMPORTANCE_NONE
            "medium" -> if (isAppRunning) NotificationManagerCompat.IMPORTANCE_NONE else NotificationManagerCompat.IMPORTANCE_DEFAULT
            else -> NotificationManagerCompat.IMPORTANCE_HIGH
        }

        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val noRefreshContentDeeplinks = listOf(
                R.string.deeplink_home,
                R.string.deeplink_services,
                R.string.deeplink_polls_list,
                R.string.deeplink_defect_reporter,
                R.string.deeplink_egov
            )
            val isNoRefreshContentDeeplink = noRefreshContentDeeplinks.any { deepLinkUrl == getString(it) }
            if (isNoRefreshContentDeeplink || deepLinkUrl.contains(EVENT_DEEP_LINK_URI_IDENTIFIER)) {
                this.data = Uri.parse(deepLinkUrl)
                setClass(applicationContext, MainActivity::class.java)
            } else if (isAppRunning && deepLinkUrl.contains(INFOBOX_DEEP_LINK_URI_IDENTIFIER)) {
                globalData.refreshContent().subscribe()
                this.data = Uri.parse(deepLinkUrl)
                setClass(applicationContext, MainActivity::class.java)
            } else {
                globalData.refreshContent().subscribe()
                this.data = Uri.parse(deepLinkUrl)
                //this.action = Intent.ACTION_VIEW
                //TODO: This fixes the issue of multiple app instances. Need to investigate
                setClass(applicationContext, MainActivity::class.java)
            }
        }

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_citykey_notification_icon_small)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_citykey_notification_icon_large))
            .setContentTitle(notificationTitle)
            .setContentText(notificationMessage)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationMessage))
            .setPriority(importance)
            .setAutoCancel(true)

        createNotificationChannel(builder, importance)
    }

    private fun createNotificationChannel(builder: NotificationCompat.Builder, importance: Int) {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "osca_notification", importance)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify((System.currentTimeMillis() % 1000).toInt(), builder.build())
    }

}
