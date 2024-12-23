package com.telekom.citykey.domain.mock

import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.contracts.CitykeyWidgetAuthApi
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.models.waste_calendar.GetSelectedWastePickupsResponse
import com.telekom.citykey.models.waste_calendar.WasteCalendarResponse

private const val GET_WASTE_CALENDAR_PICKUP_IDS = "get_selected_waste_pickups"
private const val GET_SELECTED_WASTE_PICKUPS_DATA = "get_waste_calendar"

class CitykeyWidgetAuthApiMockImpl(
    private val assetResponseMocker: AssetResponseMocker
) : CitykeyWidgetAuthApi {

    override suspend fun getWasteCalendarPickupIds(
        cityId: Int,
        actionName: String
    ): OscaResponse<GetSelectedWastePickupsResponse> = assetResponseMocker.getOscaResponseOf(
        GET_WASTE_CALENDAR_PICKUP_IDS
    )

    override suspend fun getSelectedWastePickupsData(
        wasteCalendarRequest: WasteCalendarRequest,
        cityId: Int,
        actionName: String
    ): OscaResponse<WasteCalendarResponse> = assetResponseMocker.getOscaResponseOf(
        GET_SELECTED_WASTE_PICKUPS_DATA
    )
}
