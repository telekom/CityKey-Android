/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.domain.city.weather.WeatherState
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.models.content.City
import com.telekom.citykey.models.content.CityConfig
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Date

class CityRepository(
    private val api: SmartCityApi,
    private val authApi: SmartCityAuthApi
) {

    fun getWeather(city: City): Observable<WeatherState> = api.getCityWeather(city.cityId)
        .subscribeOn(Schedulers.io())
        .map<WeatherState> { response ->
            val currentDate = Date()
            val cityNightPicturePresent = city.cityNightPicture.isNullOrEmpty().not()
            val timeIsBeforeSunrise = response.content[0].sunrise?.let { currentDate <= it } ?: false
            val timeIsAfterSunset = response.content[0].sunset?.let { currentDate >= it } ?: false
            val image =
                if (cityNightPicturePresent && (timeIsBeforeSunrise || timeIsAfterSunset))
                    city.cityNightPicture
                else
                    city.cityPicture
            WeatherState.Success(response.content[0], image.toString())
        }
        .onErrorReturn { WeatherState.Error(city.cityPicture ?: "") }
        .toObservable()
        .observeOn(AndroidSchedulers.mainThread())

    fun getCity(cityId: Int, availableCity: AvailableCity? = null) = api.getCityById(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content.firstOrNull() ?: getEmptyCityObject(availableCity) }
        .observeOn(AndroidSchedulers.mainThread())

    fun getNews(cityId: Int) = api.getCityContent(cityId)
        .subscribeOn(Schedulers.io())
        .map { response -> response.content.sortedByDescending { it.contentCreationDate } }
        .map { NewsState.Success(it) }
        .observeOn(AndroidSchedulers.mainThread())

    fun getAllCities() = api.getAllCities()
        .subscribeOn(Schedulers.io())
        .map { it.content.sortedBy { city -> city.cityName } }

    fun getServices(city: City) = api.getCityServices(city.cityId)
        .subscribeOn(Schedulers.io())
        .map { response -> response.content to city }

    fun getNearestCity(latLng: LatLng) = api.getNearestCity(latLng.longitude.toString(), latLng.latitude.toString())
        .subscribeOn(Schedulers.io())
        .map { it.content[0] }

    fun getEvents(
        cityId: Int,
        start: String? = null,
        end: String? = null,
        pageNo: Int? = null,
        pageSize: Int? = null,
        categories: ArrayList<Int>? = null,
        eventId: String? = "0",
    ) =
        api.getCityEvents(cityId, start, end, pageNo, pageSize, categories, eventId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getEventsCount(cityId: Int, start: String?, end: String?, categories: ArrayList<Int>?) =
        api.getCityEventsCount(cityId, start, end, categories)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getAllEventCategories(cityId: Int) = api.getAllEventCategories(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }
        .observeOn(AndroidSchedulers.mainThread())

    fun setEventFavored(markFavorite: Boolean, eventId: Long, cityId: Int): Completable =
        authApi.setEventFavored(markFavorite, eventId, cityId)
            .subscribeOn(Schedulers.io())

    fun getFavoredEvents(cityId: Int) = authApi.getFavoredEvents(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    private fun getEmptyCityObject(availableCity: AvailableCity?) = City(
        cityId = availableCity?.cityId ?: -1,
        cityName = availableCity?.cityName,
        cityColor = availableCity?.cityColor ?: "#1E73D2",
        stateName = availableCity?.stateName,
        country = availableCity?.country,
        cityPicture = availableCity?.cityPicture,
        cityPreviewPicture = availableCity?.cityPreviewPicture,
        cityNightPicture = null,
        servicePicture = availableCity?.servicePicture,
        municipalCoat = availableCity?.municipalCoat,
        serviceDesc = null,
        imprintDesc = null,
        imprintImage = null,
        imprintLink = null,
        latitude = 0.0,
        longitude = 0.0,
        postalCode = availableCity?.postalCode?.firstOrNull(),
        cityConfig = CityConfig()
    )
}
