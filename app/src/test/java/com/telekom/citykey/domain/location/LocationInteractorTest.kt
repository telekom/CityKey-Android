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

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LocationInteractorTest {

    @MockK
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @MockK
    private lateinit var locationTask: Task<Location>

    private lateinit var locationInteractor: LocationInteractor

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        locationInteractor = LocationInteractor(fusedLocationClient)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getLocation should return LatLng when location request is successful`() {
        // Given
        val mockLocation = mockk<Location> {
            every { latitude } returns 52.520008
            every { longitude } returns 13.404954
        }
        val expectedLatLng = LatLng(52.520008, 13.404954)

        // Mock the task completion listener
        every { locationTask.isSuccessful } returns true
        every { locationTask.result } returns mockLocation

        // Mock the FusedLocationClient.getCurrentLocation call to capture the completion listener
        val onCompleteListenerSlot = slot<OnCompleteListener<Location>>()
        every {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        } returns locationTask
        every {
            locationTask.addOnCompleteListener(capture(onCompleteListenerSlot))
        } answers {
            // Immediately execute the onComplete callback
            onCompleteListenerSlot.captured.onComplete(locationTask)
            locationTask
        }

        // When
        val testObserver = locationInteractor.getLocation().test()

        // Then
        testObserver.assertValue(expectedLatLng)
        testObserver.assertComplete()

        verify {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        }
    }

    @Test
    fun `getLocation should emit error when location request fails`() {
        // Given
        every { locationTask.isSuccessful } returns false
        every { locationTask.result } returns null

        // Mock the FusedLocationClient.getCurrentLocation call to capture the completion listener
        val onCompleteListenerSlot = slot<OnCompleteListener<Location>>()
        every {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        } returns locationTask
        every {
            locationTask.addOnCompleteListener(capture(onCompleteListenerSlot))
        } answers {
            // Immediately execute the onComplete callback
            onCompleteListenerSlot.captured.onComplete(locationTask)
            locationTask
        }

        // When
        val testObserver = locationInteractor.getLocation().test()

        // Then
        testObserver.assertError(Exception::class.java)

        verify {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        }
    }

    @Test
    fun `getLocation should emit error when location request is successful but result is null`() {
        // Given
        every { locationTask.isSuccessful } returns true
        every { locationTask.result } returns null

        // Mock the FusedLocationClient.getCurrentLocation call to capture the completion listener
        val onCompleteListenerSlot = slot<OnCompleteListener<Location>>()
        every {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        } returns locationTask
        every {
            locationTask.addOnCompleteListener(capture(onCompleteListenerSlot))
        } answers {
            // Immediately execute the onComplete callback
            onCompleteListenerSlot.captured.onComplete(locationTask)
            locationTask
        }

        // When
        val testObserver = locationInteractor.getLocation().test()

        // Then
        testObserver.assertError(Exception::class.java)

        verify {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        }
    }

    @Test
    fun `getLocation should not emit when observer is disposed`() {
        // Given
        val mockLocation = mockk<Location> {
            every { latitude } returns 52.520008
            every { longitude } returns 13.404954
        }

        // Mock the task completion listener
        every { locationTask.isSuccessful } returns true
        every { locationTask.result } returns mockLocation

        // Mock the FusedLocationClient.getCurrentLocation call but don't trigger the callback yet
        val onCompleteListenerSlot = slot<OnCompleteListener<Location>>()
        every {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        } returns locationTask
        every {
            locationTask.addOnCompleteListener(capture(onCompleteListenerSlot))
        } returns locationTask

        // When
        val testObserver = locationInteractor.getLocation().test()

        // Dispose the observer before the callback is triggered
        testObserver.dispose()

        // Then trigger the callback
        onCompleteListenerSlot.captured.onComplete(locationTask)

        // Then
        testObserver.assertNoValues()
        testObserver.assertNotTerminated()

        verify {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        }
    }

    @Test
    fun `cancelLocation should cancel the cancellation token`() {
        // Given
        val cancellationTokenSourceField = LocationInteractor::class.java
            .getDeclaredField("cancellationTokenSource")
        cancellationTokenSourceField.isAccessible = true

        val cancellationTokenSource = mockk<CancellationTokenSource>(relaxed = true)
        cancellationTokenSourceField.set(locationInteractor, cancellationTokenSource)

        // When
        locationInteractor.cancelLocation()

        // Then
        verify { cancellationTokenSource.cancel() }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `getLocation should request location with high accuracy priority`() {
        // Given
        every {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        } returns locationTask

        every { locationTask.addOnCompleteListener(any()) } returns locationTask

        // When
        locationInteractor.getLocation().test()

        // Then
        verify {
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                any<CancellationToken>()
            )
        }
    }

    @Test
    fun `getLocation should use the cancellation token from the source`() {
        // Given
        val cancellationTokenSourceField = LocationInteractor::class.java
            .getDeclaredField("cancellationTokenSource")
        cancellationTokenSourceField.isAccessible = true

        val cancellationTokenSource = mockk<CancellationTokenSource> {
            every { token } returns mockk()
        }
        cancellationTokenSourceField.set(locationInteractor, cancellationTokenSource)

        every {
            fusedLocationClient.getCurrentLocation(
                any<Int>(),
                cancellationTokenSource.token
            )
        } returns locationTask

        every { locationTask.addOnCompleteListener(any()) } returns locationTask

        // When
        locationInteractor.getLocation().test()

        // Then
        verify {
            fusedLocationClient.getCurrentLocation(
                any<Int>(),
                cancellationTokenSource.token
            )
        }
    }
}
