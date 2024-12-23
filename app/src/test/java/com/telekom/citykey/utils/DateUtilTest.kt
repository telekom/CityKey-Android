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
