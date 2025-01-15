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

package com.telekom.citykey.view.widget.waste_calendar.medium_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.telekom.citykey.R
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.isInCurrentMonth
import com.telekom.citykey.utils.extensions.removeMidnightUpdateForWidget
import com.telekom.citykey.utils.extensions.scheduleMidnightUpdateForWidget
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.widget.waste_calendar.WasteCalendarWidgetConstants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

class MediumWasteCalendarWidget : AppWidgetProvider(), KoinComponent {
    companion object {
        const val MEDIUM_WASTE_CALENDAR_WIDGET_REQUEST_CODE = 6754287
    }

    val adjustManager: AdjustManager by inject()

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            WasteCalendarWidgetConstants.ACTION_WASTE_CALENDAR_WIDGET_SCHEDULED_UPDATE,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids =
                    manager.getAppWidgetIds(context?.let { ComponentName(it, MediumWasteCalendarWidget::class.java) })
                manager.notifyAppWidgetViewDataChanged(ids, R.id.todayPickupList)
                manager.notifyAppWidgetViewDataChanged(ids, R.id.tomorrowPickupList)
                manager.notifyAppWidgetViewDataChanged(ids, R.id.dayAfterTomorrowPickupList)
                onUpdate(context!!, manager, ids)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.medium_waste_calendar_widget)

            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val dayAfterTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
            val datDateFormat = if (Locale.getDefault().language == "de") {
                SimpleDateFormat("dd.MM.yy", Locale.GERMAN)
            } else {
                SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)
            }
            views.setTextViewText(R.id.dayAfterTomorrowLabelTextView, datDateFormat.format(dayAfterTomorrow.time))
            views.setViewVisibility(R.id.todayGradient, View.VISIBLE)
            views.setViewVisibility(R.id.tomorrowGradient, View.VISIBLE)
            views.setViewVisibility(R.id.dATGradient, View.VISIBLE)
            val intentTodayListRemoteService =
                Intent(context, TodayPickupsRemoteViewService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
            val intentTomorrowListRemoteService =
                Intent(context, TomorrowPickupsRemoteViewService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
            val intentDayAfterTomorrowListRemoteService =
                Intent(context, DATPickupsRemoteViewService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }

            views.setEmptyView(R.id.todayPickupList, R.id.emptyViewForTodayPickupsUnavailable)
            views.setEmptyView(R.id.tomorrowPickupList, R.id.emptyViewForTomorrowPickupsUnavailable)
            views.setEmptyView(R.id.dayAfterTomorrowPickupList, R.id.emptyViewForDATPickupsUnavailable)

            views.setRemoteAdapter(R.id.todayPickupList, intentTodayListRemoteService)
            views.setRemoteAdapter(R.id.tomorrowPickupList, intentTomorrowListRemoteService)
            views.setRemoteAdapter(R.id.dayAfterTomorrowPickupList, intentDayAfterTomorrowListRemoteService)

            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED, true)
                data = Uri.parse("citykey://services/waste/overview/false/")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                MEDIUM_WASTE_CALENDAR_WIDGET_REQUEST_CODE,
                activityIntent,
                PendingIntent.FLAG_MUTABLE
            )

            views.setPendingIntentTemplate(R.id.todayPickupList, pendingIntent)
            views.setOnClickPendingIntent(R.id.todayPickupContainer, pendingIntent)

            val activityIntentForNextMonthNavigation = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED, true)
                data = Uri.parse("citykey://services/waste/overview/true/")
            }
            val pendingIntentForNextMonthNavigation = PendingIntent.getActivity(
                context,
                MEDIUM_WASTE_CALENDAR_WIDGET_REQUEST_CODE,
                activityIntentForNextMonthNavigation,
                PendingIntent.FLAG_MUTABLE
            )
            if (tomorrow.isInCurrentMonth().not()) {
                views.setOnClickPendingIntent(R.id.dayAfterTomorrowPickupContainer, pendingIntentForNextMonthNavigation)
                views.setPendingIntentTemplate(R.id.dayAfterTomorrowPickupList, pendingIntentForNextMonthNavigation)
                views.setOnClickPendingIntent(R.id.tomorrowPickupContainer, pendingIntentForNextMonthNavigation)
                views.setPendingIntentTemplate(R.id.tomorrowPickupList, pendingIntentForNextMonthNavigation)
            } else if (dayAfterTomorrow.isInCurrentMonth().not()) {
                views.setOnClickPendingIntent(R.id.dayAfterTomorrowPickupContainer, pendingIntentForNextMonthNavigation)
                views.setPendingIntentTemplate(R.id.dayAfterTomorrowPickupList, pendingIntentForNextMonthNavigation)
                views.setOnClickPendingIntent(R.id.tomorrowPickupContainer, pendingIntent)
                views.setPendingIntentTemplate(R.id.tomorrowPickupList, pendingIntent)
            } else {
                views.setOnClickPendingIntent(R.id.dayAfterTomorrowPickupContainer, pendingIntent)
                views.setPendingIntentTemplate(R.id.dayAfterTomorrowPickupList, pendingIntent)
                views.setOnClickPendingIntent(R.id.tomorrowPickupContainer, pendingIntent)
                views.setPendingIntentTemplate(R.id.tomorrowPickupList, pendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        adjustManager.trackEvent(R.string.waste_calendar_medium_widget)
        context.scheduleMidnightUpdateForWidget<MediumWasteCalendarWidget>(
            WasteCalendarWidgetConstants.ACTION_WASTE_CALENDAR_WIDGET_SCHEDULED_UPDATE
        )
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is removed
        adjustManager.trackEvent(R.string.remove_waste_calendar_medium_widget)
        context.removeMidnightUpdateForWidget<MediumWasteCalendarWidget>(
            WasteCalendarWidgetConstants.ACTION_WASTE_CALENDAR_WIDGET_SCHEDULED_UPDATE
        )
    }
}
