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

package com.telekom.citykey.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    const val FORMAT_YYYY_MM_DD = "yyyy-MM-dd"
    const val FORMAT_DD_MM_YYYY = "dd.MM.yyyy"
    const val FORMAT_DD_MMMM_YYYY = "dd'.' MMMM yyyy"

    fun formatTimestampDate(timestamp: Long, format: String = FORMAT_DD_MM_YYYY): String =
        SimpleDateFormat(format, Locale.getDefault())
            .format(timestamp)

    fun stringToDate(date: String, format: String = FORMAT_DD_MM_YYYY): Date =
        if (date.isNotEmpty()) {
            try {
                SimpleDateFormat(format, Locale.getDefault()).parse(date)
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }

    fun stringToCalendar(date: String): Calendar {
        val calendar = Calendar.getInstance()
        if (date.isNotEmpty()) {
            calendar.time = stringToDate(date)
        }
        return calendar
    }

    fun calendarToDateString(
        calendar: Calendar,
        simpleDateFormat: SimpleDateFormat = SimpleDateFormat(FORMAT_DD_MM_YYYY, Locale.GERMANY)
    ): String = simpleDateFormat.format(calendar.time)
}
