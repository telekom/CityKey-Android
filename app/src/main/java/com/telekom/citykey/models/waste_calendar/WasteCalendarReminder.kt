package com.telekom.citykey.models.waste_calendar

data class WasteCalendarReminder(
    val wasteTypeId: Int,
    val remindTime: String,
    val sameDay: Boolean,
    val oneDayBefore: Boolean,
    val twoDaysBefore: Boolean
)
