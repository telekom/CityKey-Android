package com.telekom.citykey.models

import android.graphics.Color
import java.util.*

sealed class WasteItems {
    class DayItem(val date: Date) : WasteItems()
    class WasteItem(
        val wasteType: String,
        private val color: String,
        val wasteTypeId: Int
    ) : WasteItems() {
        val wasteIconColorInt: Int get() = Color.parseColor(color)
        var hasReminder: Boolean = false
    }
}

data class Pickups(
    val today: List<WasteItems.WasteItem> = emptyList(),
    val tomorrow: List<WasteItems.WasteItem> = emptyList(),
    val dAT: List<WasteItems.WasteItem> = emptyList()
)
