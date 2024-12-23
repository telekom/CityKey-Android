package com.telekom.citykey.models.content

import java.util.Date

data class CityWeather(
    val atmosphericPressure: Int,
    val cityId: Int,
    val cityKey: Int,
    val cityName: String,
    val cloudiness: Int,
    val clouds: Int,
    val description: String,
    val humidity: Int,
    val maximumTemperature: Double,
    val minimumTemperature: Double,
    val pressure: Int,
    val rain: Int,
    val rainVolume: Int,
    val sunrise: Date? = null,
    val sunset: Date? = null,
    val temp: Double,
    val tempMax: Double,
    val tempMin: Double,
    val temperature: Double,
    val visibility: Int,
    val weatherCondition: String,
    val windDeg: Int,
    val windDirection: Int,
    val windSpeed: Number
)
