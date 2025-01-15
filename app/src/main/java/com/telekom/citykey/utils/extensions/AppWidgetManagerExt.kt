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

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.telekom.citykey.R
import com.telekom.citykey.view.widget.news.news_list.NewsListWidget
import com.telekom.citykey.view.widget.news.news_single_item.NewsSingleItemWidget
import com.telekom.citykey.view.widget.waste_calendar.medium_widget.MediumWasteCalendarWidget
import com.telekom.citykey.view.widget.waste_calendar.small_widget.SmallWasteCalendarWidget
import com.telekom.citykey.view.widget.waste_calendar.widget_2x1.WasteCalendarWidget2x1
import com.telekom.citykey.view.widget.waste_calendar.widget_5x1.WasteCalendarWidget5x1

fun AppWidgetManager.updateNewsWidget(context: Context) {
    val listNewsComponent = ComponentName(context.applicationContext, NewsListWidget::class.java)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(listNewsComponent), R.id.newsListView)

    val singleNewsComponent = ComponentName(context.applicationContext, NewsSingleItemWidget::class.java)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(singleNewsComponent), R.id.newsSingleItemWidgetListView)
}

fun AppWidgetManager.updateWasteCalendarWidget(context: Context) {
    val smallWasteCalenderComponent = ComponentName(context.applicationContext, SmallWasteCalendarWidget::class.java)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(smallWasteCalenderComponent), R.id.wasteList)

    val wasteCalendarWidget2x1Component = ComponentName(context.applicationContext, WasteCalendarWidget2x1::class.java)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(wasteCalendarWidget2x1Component), R.id.wasteList)

    val mediumWasteCalenderComponent = ComponentName(context.applicationContext, MediumWasteCalendarWidget::class.java)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(mediumWasteCalenderComponent), R.id.todayPickupList)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(mediumWasteCalenderComponent), R.id.tomorrowPickupList)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(mediumWasteCalenderComponent), R.id.dayAfterTomorrowPickupList)

    val wasteCalendarWidget5x1Component = ComponentName(context.applicationContext, WasteCalendarWidget5x1::class.java)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(wasteCalendarWidget5x1Component), R.id.todayPickupList)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(wasteCalendarWidget5x1Component), R.id.tomorrowPickupList)
    notifyAppWidgetViewDataChanged(getAppWidgetIds(wasteCalendarWidget5x1Component), R.id.dayAfterTomorrowPickupList)
}
