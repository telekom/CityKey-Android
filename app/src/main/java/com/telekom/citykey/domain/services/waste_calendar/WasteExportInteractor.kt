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

package com.telekom.citykey.domain.services.waste_calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.provider.CalendarContract.*
import com.telekom.citykey.common.TimeConstants.MILLIS_IN_ONE_DAY
import com.telekom.citykey.networkinterface.models.waste_calendar.CalendarAccount
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarPickups
import com.telekom.citykey.utils.extensions.isInPast
import com.telekom.citykey.utils.extensions.isToday
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class WasteExportInteractor(private val contentResolver: ContentResolver) {

    private val calendarInfo = mutableListOf<CalendarAccount>()

    private val eventProjections = arrayOf(
        Calendars._ID,
        Calendars.ACCOUNT_NAME,
        Calendars.CALENDAR_DISPLAY_NAME,
        Calendars.CALENDAR_COLOR,
    )

    companion object {
        private const val PROJECTION_ID_INDEX = 0
        private const val PROJECTION_CALENDAR_ACCOUNT = 1
        private const val PROJECTION_DISPLAY_NAME_INDEX = 2
        private const val PROJECTION_COLOR_INDEX = 3
    }

    fun getCalendarsInfo(): Single<List<CalendarAccount>> = if (calendarInfo.isEmpty()) {
        Single.create<List<CalendarAccount>?> { it.onSuccess(calendarInfo) }
            .subscribeOn(Schedulers.computation())
            .map { getCalendarInf() }
            .observeOn(AndroidSchedulers.mainThread())
    } else Single.just(calendarInfo)

    fun exportCalendarEvents(
        list: MutableList<WasteCalendarPickups>,
        account: CalendarAccount,
        filters: List<String>
    ): Single<Int> = Single.just<List<WasteCalendarPickups>>(list)
        .subscribeOn(Schedulers.computation())
        .map { filterWastCalendarPickups(it, filters) }
        .map { createContentValues(it, account.calId) }
        .map { contentResolver.bulkInsert(Events.CONTENT_URI, it.toTypedArray()) }
        .observeOn(AndroidSchedulers.mainThread())

    private fun filterWastCalendarPickups(
        wasteCalendar: List<WasteCalendarPickups>,
        filters: List<String>
    ): List<WasteCalendarPickups> {
        val listItems = mutableListOf<WasteCalendarPickups>()
        wasteCalendar.filter { !it.date.isInPast || it.date.isToday }
            .forEach {
                val data = it.wasteTypeList.filter { wasteItems ->
                    filters.contains(wasteItems.wasteTypeId.toString())
                }
                listItems.add(WasteCalendarPickups(it.date, data))
            }
        return listItems
    }

    private fun createContentValues(pickups: List<WasteCalendarPickups>, calendarId: Long): List<ContentValues> {

        val eventsValues = mutableListOf<ContentValues>()
        pickups.forEach {
            it.wasteTypeList.forEach { wasteItem ->
                val values = ContentValues()
                values.put(EventsEntity.CALENDAR_ID, calendarId)
                val eventTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                eventTime.time = it.date
                eventTime.set(Calendar.HOUR, 12)
                values.put(EventsEntity.DTSTART, eventTime.timeInMillis)
                values.put(EventsEntity.DTEND, eventTime.timeInMillis + MILLIS_IN_ONE_DAY)
                values.put(EventsEntity.ALL_DAY, 1)
                values.put(Events.EVENT_TIMEZONE, eventTime.timeZone.id)
                values.put(EventsEntity.TITLE, wasteItem.wasteType)
                eventsValues.add(values)
            }
        }
        return eventsValues
    }

    private fun getCalendarInf(): List<CalendarAccount> {
        val cur: Cursor? =
            contentResolver.query(
                Calendars.CONTENT_URI,
                eventProjections,
                null,
                null,
                null
            )
        if ((cur != null) && (cur.count > 0)) {
            while (cur.moveToNext()) {
                val calID = cur.getLong(PROJECTION_ID_INDEX)
                val displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val color = cur.getInt(PROJECTION_COLOR_INDEX)
                val accountName = cur.getString(PROJECTION_CALENDAR_ACCOUNT)
                calendarInfo.add(CalendarAccount(calID, color, displayName, accountName))
            }
        }
        cur?.close()
        return calendarInfo
    }
}
