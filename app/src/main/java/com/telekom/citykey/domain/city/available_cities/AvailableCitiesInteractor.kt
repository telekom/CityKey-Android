package com.telekom.citykey.domain.city.available_cities

import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.AvailableCity
import io.reactivex.Maybe

class AvailableCitiesInteractor(private val cityRepository: CityRepository) {

    private val _availableCities: MutableList<AvailableCity> = mutableListOf()

    val availableCities: Maybe<List<AvailableCity>>
        get() =
            if (_availableCities.isEmpty())
                cityRepository.getAllCities()
                    .doOnSuccess { cities ->
                        _availableCities.clear()
                        _availableCities.addAll(cities)
                    }
            else
                Maybe.just(_availableCities)

    fun clearAvailableCities() = _availableCities.clear()
}
