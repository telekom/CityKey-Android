package com.telekom.citykey.domain.services.waste_calendar

import com.telekom.citykey.models.waste_calendar.FtuWaste

sealed class WasteAddressState {
    class Success(val items: List<FtuWaste>) : WasteAddressState()
    class Error(val throwable: Throwable) : WasteAddressState()
}
