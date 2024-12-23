package com.telekom.citykey.domain.city.weather

import android.annotation.SuppressLint
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class WeatherInteractor(private val cityRepository: CityRepository, private val globalData: GlobalData) {

    private val _weatherSubject: BehaviorSubject<WeatherState> = BehaviorSubject.create()
    val weatherData: Observable<WeatherState> = _weatherSubject.hide()

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        globalData.city
            .doOnEach { _weatherSubject.onNext(WeatherState.Loading) }
            .flatMap { cityRepository.getWeather(it) }
            .subscribe(_weatherSubject::onNext, Timber::e)
    }
}
