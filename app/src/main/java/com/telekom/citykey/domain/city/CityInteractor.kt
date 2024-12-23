package com.telekom.citykey.domain.city

import android.annotation.SuppressLint
import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.models.content.City
import com.telekom.citykey.utils.PreferencesHelper
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class CityInteractor(
    private val cityRepository: CityRepository, private val preferencesHelper: PreferencesHelper
) {

    companion object {
        var cityColorInt = 0
    }

    private val _city: BehaviorSubject<City> = BehaviorSubject.create()
    val city: Observable<City> = _city.hide()

    val currentCityId get() = _city.value?.cityId ?: -1
    val cityColor get() = Color.parseColor(_city.value?.cityColor ?: "")
    val cityName get() = _city.value?.cityName ?: ""
    val cityLocation get() = _city.value?.location ?: LatLng(0.0, 0.0)

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        city.subscribe {
            cityColorInt = it.cityColorInt
            preferencesHelper.setSelectedCityId(it.cityId)
            preferencesHelper.setSelectedCityName(it.cityName)
        }
    }

    fun loadCity(cityId: Int = currentCityId): Maybe<City> {
        return if (!preferencesHelper.isFirstTime) {
            cityRepository.getCity(cityId).doOnSuccess(_city::onNext)
        } else {
            Maybe.empty()
        }
    }

    fun loadCity(availableCity: AvailableCity): Maybe<City> =
        cityRepository.getCity(availableCity.cityId, availableCity).doOnSuccess(_city::onNext)
}