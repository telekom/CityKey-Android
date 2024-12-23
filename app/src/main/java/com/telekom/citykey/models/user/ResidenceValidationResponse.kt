package com.telekom.citykey.models.user

data class ResidenceValidationResponse(
    val postalCodeMessage: String,
    val cityName: String,
    val homeCityId: Int
)
