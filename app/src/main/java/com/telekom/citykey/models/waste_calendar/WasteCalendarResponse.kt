package com.telekom.citykey.models.waste_calendar

class WasteCalendarResponse(
    val calendar: List<WasteCalendarPickups>,
    val address: WasteCalendarAddress,
    val reminders: List<WasteCalendarReminder>
)
