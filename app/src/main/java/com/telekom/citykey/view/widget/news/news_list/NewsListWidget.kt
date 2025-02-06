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

package com.telekom.citykey.view.widget.news.news_list

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.telekom.citykey.R
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.view.main.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NewsListWidget : AppWidgetProvider(), KoinComponent {

    companion object {
        const val NEWS_IMAGE_SIZE = 512
        const val NEWS_LIST_WIDGET_REQUEST_CODE = 7834519
        const val ITEM_CORNER_RADIUS = 16
    }

    private val adjustManager: AdjustManager by inject()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.news_list_widget)
            val intent = Intent(context, NewsListWidgetRemoteViewService::class.java).apply {
                putExtra("widgetID", appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setRemoteAdapter(R.id.newsListView, intent)
            views.setEmptyView(R.id.newsListView, R.id.loadingIndicatorImageView)
            views.setImageViewResource(R.id.loadingIndicatorImageView, R.drawable.bg_news_list_widget_loader)
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, NEWS_LIST_WIDGET_REQUEST_CODE, activityIntent,
                PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.newsListView, pendingIntent)
            views.setOnClickPendingIntent(R.id.loadingIndicatorImageView, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        if (NetworkConnection.checkInternetConnection(context)) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.newsListView)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        adjustManager.trackEvent(R.string.news_medium_widget)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        adjustManager.trackEvent(R.string.remove_news_medium_widget)
    }
}
