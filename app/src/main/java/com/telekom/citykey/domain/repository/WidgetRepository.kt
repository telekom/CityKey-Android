package com.telekom.citykey.domain.repository

import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.api.contracts.CitykeyWidgetApi
import com.telekom.citykey.models.api.contracts.CitykeyWidgetAuthApi
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.extensions.isInPast
import com.telekom.citykey.utils.extensions.isToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetRepository(
    private val citykeyWidgetApi: CitykeyWidgetApi,
    private val citykeyWidgetAuthApi: CitykeyWidgetAuthApi,
    private val preferencesHelper: PreferencesHelper
) {

    val currentCityId get() = preferencesHelper.getSelectedCityId()

    suspend fun getNewsForCurrentCity(count: Int): NewsState = withContext(Dispatchers.IO) {
        try {
            val oscaResponse = citykeyWidgetApi.getNewsForCityContent(currentCityId)
            val filteredList =
                if (oscaResponse.content.size > count) oscaResponse.content.take(count) else oscaResponse.content
            NewsState.Success(filteredList)
        } catch (e: Exception) {
            NewsState.Error
        }
    }

    suspend fun getSelectedWastePickupIds() = withContext(Dispatchers.IO) {
        try {
            val oscaResponse = citykeyWidgetAuthApi.getWasteCalendarPickupIds(currentCityId)
            oscaResponse.content.wasteTypeIds
            //   NewsState.Success()
        } catch (e: Exception) {
            e
        }
    }

    suspend fun getWasteCalendarData() = withContext(Dispatchers.IO) {
        try {
            val oscaResponse = citykeyWidgetAuthApi.getSelectedWastePickupsData(
                WasteCalendarRequest("", ""),
                currentCityId
            )
            oscaResponse.content.calendar.filter { !it.date.isInPast || it.date.isToday }.take(3)
        } catch (e: Exception) {
            e
        }
    }
}
