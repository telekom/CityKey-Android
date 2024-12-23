package com.telekom.citykey.domain.location

import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.NearestCity
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

class LocationBasedCitiesInteractor(
    private val cityRepository: CityRepository,
    private val locationInteractor: LocationInteractor
) {

    fun getNearestCity(): Maybe<NearestCity> =
        locationInteractor.getLocation()
            .subscribeOn(Schedulers.io())
            .flatMapMaybe(cityRepository::getNearestCity)
}
