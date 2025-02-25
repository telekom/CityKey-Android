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

package com.telekom.citykey.utils

import com.telekom.citykey.utils.DateUtil.FORMAT_YYYY_MM_DD
import com.telekom.citykey.utils.DateUtil.formatTimestampDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Date

class DateUtilTest {

    @Test
    fun stringToDate_should_convert_date_string() {
        val date = DateUtil.stringToDate("2018-10-14", FORMAT_YYYY_MM_DD)
        assertNotNull(date)
        val calendar = Calendar.getInstance()
        calendar.time = date
        assertEquals(14, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, calendar.get(Calendar.MONTH))
        assertEquals(2018, calendar.get(Calendar.YEAR))
    }

    @Test
    fun stringToDate_date_is_empty_should_return_current_date() {
        val date = DateUtil.stringToDate("", FORMAT_YYYY_MM_DD)
        val currentDate = Date()
        assertEquals(currentDate, date)
    }

    @Test
    fun `test formatTimestampDate_with_custom_format`() {
        val timestamp = 1627776000000L // 1st August 2021, 00:00:00 GMT
        val expectedDate = "01/08/2021"
        val formattedDate = formatTimestampDate(timestamp)
        assertNotEquals(expectedDate, formattedDate)
    }

    @Test
    fun `test formatTimestampDate_with_YYYYMMDD_format`() {
        val timestamp = 1627776000000L // 1st August 2021, 00:00:00 GMT
        val customFormat = "yyyy-MM-dd"
        val expectedDate = "2021-08-01"
        val formattedDate = formatTimestampDate(timestamp, customFormat)
        assertEquals(expectedDate, formattedDate)
    }

    @Test
    fun formatDate_should_format_date_object() {
        val date = formatTimestampDate(Date().time, FORMAT_YYYY_MM_DD)
        assertNotNull(date)
    }

    @Test
    fun dateStringToCalendar_should_convert() {
        val calendar = DateUtil.stringToCalendar("12.12.2018")
        assertEquals(2018, calendar.get(Calendar.YEAR))
        assertEquals(11, calendar.get(Calendar.MONTH))
        assertEquals(12, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun calendarToDateStrGermany_should_convert() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, 2019)
        calendar.set(Calendar.MONTH, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        assertEquals("01.01.2019", DateUtil.calendarToDateString(calendar))
    }
}
