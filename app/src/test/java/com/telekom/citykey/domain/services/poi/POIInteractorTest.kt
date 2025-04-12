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

package com.telekom.citykey.domain.services.poi

import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.networkinterface.models.content.City
import com.telekom.citykey.networkinterface.models.poi.PoiCategory
import com.telekom.citykey.networkinterface.models.poi.PoiCategoryGroup
import com.telekom.citykey.networkinterface.models.poi.PointOfInterest
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class POIInteractorTest {

    @MockK
    private lateinit var servicesRepository: ServicesRepository

    @MockK
    private lateinit var globalData: GlobalData

    @MockK
    private lateinit var prefs: PreferencesHelper

    @MockK
    private lateinit var locationInteractor: LocationInteractor

    private lateinit var poiInteractor: POIInteractor

    // Test data
    private val cityId = 10
    private val cityName = "TestCity"
    private val cityLocation = LatLng(10.0, 20.0)
    private val testCategory = PoiCategory(234, "Category 1", "icon1")
    private val testCategoryGroups = listOf(
        PoiCategoryGroup(55, "Group 1", "", listOf(testCategory))
    )
    private val testPoislt50 = listOf(
        PointOfInterest("poi23", 23.4, 56.7, "icon1", "address1", "123456", "website1", "1", "", "", 5L)
    )
    private val testPoisgt150 = List(200) {
        PointOfInterest("poi$it", 23.4, 56.7, "icon1", "address1", "123456", "website1", "1", "", "", 5L)
    }
    private val testPoisbt50150 = List(100) {
        PointOfInterest("poi$it", 23.4, 56.7, "icon1", "address1", "123456", "website1", "1", "", "", 5L)
    }
    private val citySubject = BehaviorSubject.create<City>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        // Configure RxJava schedulers for testing
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        // Setup mocks
        every { globalData.currentCityId } returns cityId
        every { globalData.cityName } returns cityName
        every { globalData.cityLocation } returns cityLocation
        every { globalData.city } returns citySubject

        // Initialize with mocked dependencies
        poiInteractor = POIInteractor(servicesRepository, globalData, prefs, locationInteractor)
    }

    @AfterEach
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
        clearAllMocks()
    }

    @Test
    fun `init subscribes to city changes`() {
        // Verify the subscription happens in init
        verify { globalData.city }

        // Emit a city change
        val testCity = City(cityId, cityName, "Country")
        citySubject.onNext(testCity)

        // Verify state is reset
        verify { globalData.cityLocation }
    }

    @Test
    fun `getCategories calls repository when cache is empty`() {
        // Setup
        every { servicesRepository.getPoiCategories(cityId) } returns Maybe.just(testCategoryGroups)

        // Action
        poiInteractor.getCategories().test()

        // Verify
        verify { servicesRepository.getPoiCategories(cityId) }
    }

    @Test
    fun `getCategories uses cache when available`() {
        // Setup - first call to populate cache
        every { servicesRepository.getPoiCategories(cityId) } returns Maybe.just(testCategoryGroups)
        poiInteractor.getCategories().test()

        // Action - second call
        poiInteractor.getCategories().test()

        // Verify repository was only called once
        verify(exactly = 1) { servicesRepository.getPoiCategories(cityId) }
    }

    @Test
    fun `getPois with initial loading calls location interactor`() {
        // Setup
        val location = LatLng(12.0, 22.0)
        every { locationInteractor.getLocation() } returns Single.just(location)
        every {
            servicesRepository.getPOIs(any(), any(), any(), any())
        } returns Maybe.just(testPoislt50)

        // Action
        poiInteractor.getPois(testCategory, true).test()

        // Verify
        verify { locationInteractor.getLocation() }
        verify { servicesRepository.getPOIs(any(), any(), any(), any()) }
    }

    @Test
    fun `getPois with initial loading empty`() {
        // Setup
        val location = LatLng(12.0, 22.0)
        every { locationInteractor.getLocation() } returns Single.just(location)
        every {
            servicesRepository.getPOIs(any(), any(), any(), any())
        } returns Maybe.just(emptyList())

        // Action
        poiInteractor.getPois(testCategory, true).test()

        // Verify
        verify { locationInteractor.getLocation() }
        verify { servicesRepository.getPOIs(any(), any(), any(), any()) }
    }

    @Test
    fun `getPois with initial loading gt 150`() {
        // Setup
        val location = LatLng(12.0, 22.0)
        every { locationInteractor.getLocation() } returns Single.just(location)
        every {
            servicesRepository.getPOIs(any(), any(), any(), any())
        } returns Maybe.just(testPoisgt150)

        // Action
        poiInteractor.getPois(testCategory, true).test()

        // Verify
        verify { locationInteractor.getLocation() }
        verify { servicesRepository.getPOIs(any(), any(), any(), any()) }
    }

    @Test
    fun `getPois with initial loading bt 50 - 150`() {
        // Setup
        val location = LatLng(12.0, 22.0)
        every { locationInteractor.getLocation() } returns Single.just(location)
        every {
            servicesRepository.getPOIs(any(), any(), any(), any())
        } returns Maybe.just(testPoisbt50150)

        // Action
        poiInteractor.getPois(testCategory, true).test()

        // Verify
        verify { locationInteractor.getLocation() }
        verify { servicesRepository.getPOIs(any(), any(), any(), any()) }
    }

    @Test
    fun `getPois handles location failure by using city location`() {
        // Setup
        every { locationInteractor.getLocation() } returns Single.error(Exception("Location unavailable"))
        every {
            servicesRepository.getPOIs(any(), any(), any(), any())
        } returns Maybe.just(testPoislt50)

        // Action
        poiInteractor.getPois(testCategory, true).test()

        // Verify
        verify { locationInteractor.getLocation() }
        verify { globalData.cityLocation }
        verify { servicesRepository.getPOIs(any(), any(), any(), any()) }
    }

    @Test
    fun `getPois without initial loading skips location fetch`() {
        // Setup - first fetch to set location
        val location = LatLng(12.0, 22.0)
        every { locationInteractor.getLocation() } returns Single.just(location)
        every {
            servicesRepository.getPOIs(any(), any(), any(), any())
        } returns Maybe.just(testPoislt50)
        poiInteractor.getPois(testCategory, true).test()

        clearMocks(locationInteractor)

        // Action - second fetch without initial loading
        poiInteractor.getPois(testCategory, false).test()

        // Verify location interactor wasn't called again
        verify(exactly = 0) { locationInteractor.getLocation() }
        verify(exactly = 2) { servicesRepository.getPOIs(any(), any(), any(), any()) }
    }

    @Test
    fun `getSelectedPoiCategory delegates to preferences`() {
        // Setup
        every { prefs.getPoiCategory(cityName) } returns testCategory

        // Action
        poiInteractor.getSelectedPoiCategory()

        // Verify
        verify { prefs.getPoiCategory(cityName) }
    }

    @Test
    fun `observable properties return the corresponding subjects`() {
        // Just verify the getters don't throw exceptions
        val poiDataObservable = poiInteractor.poiData
        val activeCategoryObservable = poiInteractor.activeCategory
        val poiStateObservable = poiInteractor.poiState

        // Basic assertion that observables are returned
        assert(poiDataObservable is Observable<*>)
        assert(activeCategoryObservable is Observable<*>)
        assert(poiStateObservable is Observable<*>)
    }
}
