package com.telekom.citykey.models.api.contracts

import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.content.CityContent
import retrofit2.http.Query

interface CitykeyWidgetApi {


    suspend fun getNewsForCityContent(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_News"
    ): OscaResponse<List<CityContent>>

}
