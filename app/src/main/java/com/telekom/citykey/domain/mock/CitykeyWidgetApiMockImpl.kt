package com.telekom.citykey.domain.mock

import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.contracts.CitykeyWidgetApi
import com.telekom.citykey.models.content.CityContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GET_NEWS_FOR_CITY_CONTENT = "get_news_for_city_content"

class CitykeyWidgetApiMockImpl(
    private val assetResponseMocker: AssetResponseMocker
) : CitykeyWidgetApi {

    override suspend fun getNewsForCityContent(
        cityId: Int,
        actionName: String
    ): OscaResponse<List<CityContent>> = withContext(Dispatchers.IO) {
        assetResponseMocker.getOscaResponseListOf(GET_NEWS_FOR_CITY_CONTENT)
    }
}
