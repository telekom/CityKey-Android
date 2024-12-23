package com.telekom.citykey.models.api.contracts

import com.telekom.citykey.domain.repository.ErrorType
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.models.waste_calendar.GetSelectedWastePickupsResponse
import com.telekom.citykey.models.waste_calendar.WasteCalendarResponse
import retrofit2.http.Body
import retrofit2.http.Query

interface CitykeyWidgetAuthApi {


    @ErrorType(OscaErrorResponse::class)
    suspend fun getWasteCalendarPickupIds(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_UserWasteType"
    ): OscaResponse<GetSelectedWastePickupsResponse>


    @ErrorType(OscaErrorResponse::class)
    suspend fun getSelectedWastePickupsData(
        @Body wasteCalendarRequest: WasteCalendarRequest,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "POST_WasteCalendarData"
    ): OscaResponse<WasteCalendarResponse>
}
