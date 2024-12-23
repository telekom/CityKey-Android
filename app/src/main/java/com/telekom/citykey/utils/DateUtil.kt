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
