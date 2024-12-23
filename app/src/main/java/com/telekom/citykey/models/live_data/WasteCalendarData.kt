package com.telekom.citykey.models.live_data

import com.telekom.citykey.models.WasteItems
import com.telekom.citykey.models.waste_calendar.WasteCalendarPickups

class WasteCalendarData(
    val listItems: List<WasteItems>,
    val monthItems: List<WasteCalendarPickups>,
    val cityColor: Int,
    val availableMonths: Int
)
