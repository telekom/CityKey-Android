package com.telekom.citykey.domain.city.weather

import com.telekom.citykey.models.content.CityWeather

sealed class WeatherState {
    object Loading : WeatherState()
    class Error(val cityPicture: String) : WeatherState()
    class Success(val content: CityWeather, val cityPicture: String) : WeatherState()
}
