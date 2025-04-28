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

package com.telekom.citykey.domain.location

import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.networkinterface.models.content.NearestCity
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class LocationBasedCitiesInteractorTest {

    @MockK
    private lateinit var cityRepository: CityRepository

    @MockK
    private lateinit var locationInteractor: LocationInteractor

    private lateinit var interactor: LocationBasedCitiesInteractor
    private lateinit var testScheduler: TestScheduler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testScheduler = TestScheduler()

        // Replace the IO scheduler with our test scheduler for controlled testing
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }

        interactor = LocationBasedCitiesInteractor(
            cityRepository,
            locationInteractor
        )
    }

    @AfterEach
    fun tearDown() {
        RxJavaPlugins.reset()
        clearAllMocks()
    }

    @Test
    fun `getNearestCity should return nearest city when location is available`() {
        // Given
        val location = LatLng(52.520008, 13.404954) // Berlin coordinates
        val expectedCity = NearestCity(8, 80)

        every { locationInteractor.getLocation() } returns Single.just(location)
        every { cityRepository.getNearestCity(location) } returns Maybe.just(expectedCity)

        // When
        val testObserver = interactor.getNearestCity().test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertValue(expectedCity)
        testObserver.assertComplete()

        verify(exactly = 1) { locationInteractor.getLocation() }
        verify(exactly = 1) { cityRepository.getNearestCity(location) }
    }

    @Test
    fun `getNearestCity should complete empty when no nearest city found`() {
        // Given
        val location = LatLng(52.520008, 13.404954)

        every { locationInteractor.getLocation() } returns Single.just(location)
        every { cityRepository.getNearestCity(location) } returns Maybe.empty()

        // When
        val testObserver = interactor.getNearestCity().test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertNoValues()
        testObserver.assertComplete()

        verify(exactly = 1) { locationInteractor.getLocation() }
        verify(exactly = 1) { cityRepository.getNearestCity(location) }
    }

    @Test
    fun `getNearestCity should propagate repository error`() {
        // Given
        val location = LatLng(52.520008, 13.404954)
        val testError = RuntimeException("Repository error")

        every { locationInteractor.getLocation() } returns Single.just(location)
        every { cityRepository.getNearestCity(location) } returns Maybe.error(testError)

        // When
        val testObserver = interactor.getNearestCity().test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertError(testError)

        verify(exactly = 1) { locationInteractor.getLocation() }
        verify(exactly = 1) { cityRepository.getNearestCity(location) }
    }

    @Test
    fun `getNearestCity should propagate location error`() {
        // Given
        val testError = RuntimeException("Location error")

        every { locationInteractor.getLocation() } returns Single.error(testError)

        // When
        val testObserver = interactor.getNearestCity().test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertError(testError)

        verify(exactly = 1) { locationInteractor.getLocation() }
        verify(exactly = 0) { cityRepository.getNearestCity(any()) }
    }

    @Test
    fun `getNearestCity should use IO scheduler for location operations`() {
        // Given
        val location = LatLng(52.520008, 13.404954)
        val expectedCity = NearestCity(8, 80)

        every { locationInteractor.getLocation() } returns Single.just(location)
        every { cityRepository.getNearestCity(location) } returns Maybe.just(expectedCity)

        // When
        val testObserver = interactor.getNearestCity().test()

        // Then verify operation hasn't completed without advancing scheduler
        testObserver.assertNoValues()

        // When we advance the test scheduler
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertValue(expectedCity)

        verify { locationInteractor.getLocation() }
        verify { cityRepository.getNearestCity(location) }
    }

    @Test
    fun `getNearestCity should chain operations correctly`() {
        // Given - simulate a delayed response from location
        val location = LatLng(52.520008, 13.404954)
        val expectedCity = NearestCity(8, 80)

        every { locationInteractor.getLocation() } returns Single.just(location)
            .delay(50, TimeUnit.MILLISECONDS, testScheduler)
        every { cityRepository.getNearestCity(location) } returns Maybe.just(expectedCity)
            .delay(50, TimeUnit.MILLISECONDS, testScheduler)

        // When
        val testObserver = interactor.getNearestCity().test()

        // Verify no values after only 40ms
        testScheduler.advanceTimeBy(40, TimeUnit.MILLISECONDS)
        testObserver.assertNoValues()

        // Advance past location but before repository response
        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
        testObserver.assertNoValues()

        // Complete the chain
        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertValue(expectedCity)
        testObserver.assertComplete()
    }
}
