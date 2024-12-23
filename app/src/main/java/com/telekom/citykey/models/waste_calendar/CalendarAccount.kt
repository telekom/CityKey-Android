package com.telekom.citykey.models.waste_calendar

import androidx.annotation.ColorInt

data class CalendarAccount(
    var calId: Long,
    @ColorInt var calendarColor: Int,
    val calendarDisplayName: String,
    val calendarAccountName: String
)
