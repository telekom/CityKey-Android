package com.telekom.citykey.models.live_data

import androidx.annotation.ColorInt
import com.telekom.citykey.models.content.City
import com.telekom.citykey.utils.extensions.tryParsingColor
import com.telekom.citykey.view.home.HomeViewTypes

data class HomeData(
    val city: String?,
    val municipalCoat: String?,
    @ColorInt val cityColor: Int,
    val viewTypes: List<Int>
) {
    companion object {
        fun fromCity(city: City): HomeData {
            val viewTypes = mutableListOf<Int>()
            viewTypes.add(HomeViewTypes.VIEW_TYPE_NEWS)
            viewTypes.add(HomeViewTypes.VIEW_TYPE_EVENTS)
//            if (city.cityConfig.showHomeTips) viewTypes.add(HomeViewTypes.VIEW_TYPE_TIPS)
//            if (city.cityConfig.showHomeOffers) viewTypes.add(HomeViewTypes.VIEW_TYPE_OFFERS)
//            if (city.cityConfig.showHomeDiscounts) viewTypes.add(HomeViewTypes.VIEW_TYPE_DISCOUNTS)

            return HomeData(
                city.cityName,
                city.municipalCoat,
                tryParsingColor(city.cityColor),
                viewTypes
            )
        }
    }
}
