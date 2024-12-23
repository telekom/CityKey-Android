package com.telekom.citykey.models.content

data class CityConfig(
    val showFavouriteServices: Boolean = false,
    val showNewServices: Boolean = false,
    val showMostUsedServices: Boolean = false,
    val showCategories: Boolean = false,
    val showOurServices: Boolean = false,
    val showHomeDiscounts: Boolean = false,
    val showHomeOffers: Boolean = false,
    val showHomeTips: Boolean = false,
    val stickyNewsCount: Int = 0,
    val eventsCount: Int = 0,
    val yourEventsCount: Int = 0,
    val showServicesOption: Boolean = false
)
