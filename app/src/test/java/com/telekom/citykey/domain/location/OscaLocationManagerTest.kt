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

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class OscaLocationManagerTest {

    @MockK
    private lateinit var geocoder: Geocoder

    @MockK
    private lateinit var mockAddress: Address

    private lateinit var oscaLocationManager: OscaLocationManager
    private lateinit var testScheduler: TestScheduler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testScheduler = TestScheduler()

        // Replace the IO scheduler with our test scheduler for controlled testing
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }

        oscaLocationManager = OscaLocationManager(geocoder)

        // Mock the Address properties
        every { mockAddress.latitude } returns 52.520008
        every { mockAddress.longitude } returns 13.404954
    }

    @AfterEach
    fun tearDown() {
        RxJavaPlugins.reset()
        clearAllMocks()
    }

    @Test
    fun `getLatLngFromAddress should return LatLng for valid address`() {
        // Given
        val addressName = "Berlin, Germany"
        val expectedLatLng = LatLng(52.520008, 13.404954)
        val addressList = listOf(mockAddress)

        every { geocoder.getFromLocationName(addressName, 1) } returns addressList

        // When
        val testObserver = oscaLocationManager.getLatLngFromAddress(addressName).test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertValue(expectedLatLng)
        testObserver.assertComplete()

        verify(exactly = 1) { geocoder.getFromLocationName(addressName, 1) }
    }

    @Test
    @Throws(IndexOutOfBoundsException::class)
    fun `getLatLngFromAddress should fail when geocoder returns empty list`() {
        // Given
        val addressName = "Invalid Address"

        every { geocoder.getFromLocationName(addressName, 1) } returns emptyList()

        // When
        val testObserver = oscaLocationManager.getLatLngFromAddress(addressName).test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertError(IndexOutOfBoundsException::class.java)

        verify(exactly = 1) { geocoder.getFromLocationName(addressName, 1) }
    }

    @Test
    fun `getLatLngFromAddress should fail when geocoder returns null`() {
        // Given
        val addressName = "Invalid Address"

        every { geocoder.getFromLocationName(addressName, 1) } returns null

        // When
        val testObserver = oscaLocationManager.getLatLngFromAddress(addressName).test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertError(NullPointerException::class.java)

        verify(exactly = 1) { geocoder.getFromLocationName(addressName, 1) }
    }

    @Test
    fun `getLatLngFromAddress should use IO scheduler`() {
        // Given
        val addressName = "Berlin, Germany"
        val addressList = listOf(mockAddress)

        every { geocoder.getFromLocationName(addressName, 1) } returns addressList

        // When
        val testObserver = oscaLocationManager.getLatLngFromAddress(addressName).test()

        // Then verify no emissions before advancing scheduler
        testObserver.assertNoValues()

        // When advancing scheduler
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then verify emission after advancing scheduler
        testObserver.assertValueCount(1)
    }

    @Test
    fun `getAddressFromLatLng should return address for valid coordinates`() {
        // Given
        val latitude = 52.520008
        val longitude = 13.404954
        val addressList = listOf(mockAddress)

        every { geocoder.getFromLocation(latitude, longitude, 1) } returns addressList

        // When
        val testObserver = oscaLocationManager.getAddressFromLatLng(latitude, longitude).test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertValue(mockAddress)
        testObserver.assertComplete()

        verify(exactly = 1) { geocoder.getFromLocation(latitude, longitude, 1) }
    }

    @Test
    fun `getAddressFromLatLng should use IO scheduler`() {
        // Given
        val latitude = 52.520008
        val longitude = 13.404954
        val addressList = listOf(mockAddress)

        every { geocoder.getFromLocation(latitude, longitude, 1) } returns addressList

        // When
        val testObserver = oscaLocationManager.getAddressFromLatLng(latitude, longitude).test()

        // Then verify no emissions before advancing scheduler
        testObserver.assertNoValues()

        // When advancing scheduler
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then verify emission after advancing scheduler
        testObserver.assertValueCount(1)
    }

    @Test
    fun `getAddressFromLatLng should handle exception from geocoder`() {
        // Given
        val latitude = 52.520008
        val longitude = 13.404954
        val exception = RuntimeException("Geocoder failed")

        every { geocoder.getFromLocation(latitude, longitude, 1) } throws exception

        // When
        val testObserver = oscaLocationManager.getAddressFromLatLng(latitude, longitude).test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertError(exception)

        verify(exactly = 1) { geocoder.getFromLocation(latitude, longitude, 1) }
    }

    @Test
    fun `getLatLngFromAddress should handle exception from geocoder`() {
        // Given
        val addressName = "Berlin, Germany"
        val exception = RuntimeException("Geocoder failed")

        every { geocoder.getFromLocationName(addressName, 1) } throws exception

        // When
        val testObserver = oscaLocationManager.getLatLngFromAddress(addressName).test()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)

        // Then
        testObserver.assertError(exception)

        verify(exactly = 1) { geocoder.getFromLocationName(addressName, 1) }
    }
}
