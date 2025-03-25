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

package com.telekom.citykey.view.services.poi

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.domain.services.poi.POIInteractor
import com.telekom.citykey.network.extensions.latLang
import com.telekom.citykey.networkinterface.models.poi.PoiCategory
import com.telekom.citykey.networkinterface.models.poi.PoiData
import com.telekom.citykey.networkinterface.models.poi.PointOfInterest
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.NetworkingViewModel
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class PoiGuideViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var poiInteractor: POIInteractor

    @MockK
    private lateinit var globalData: GlobalData

    @MockK
    private lateinit var locationInteractor: LocationInteractor

    @MockK
    private lateinit var prefs: PreferencesHelper

    @RelaxedMockK
    private lateinit var userLocationObserver: Observer<LatLng?>

    @RelaxedMockK
    private lateinit var launchCategorySelectionObserver: Observer<PoiCategory?>

    @RelaxedMockK
    private lateinit var poiDataObserver: Observer<PoiData>

    @RelaxedMockK
    private lateinit var activeCategoryObserver: Observer<PoiCategory>

    @RelaxedMockK
    private lateinit var showDetailsObserver: Observer<PointOfInterest>

    @RelaxedMockK
    private lateinit var isFirstTimeObserver: Observer<Boolean>

    @RelaxedMockK
    private lateinit var poiStateObserver: Observer<PoiState>

    private lateinit var viewModel: PoiGuideViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        // Setup RxJava to run synchronously in tests
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        // Mock the city name
        every { globalData.cityName } returns "Berlin"

        // Mock the LiveData streams from POIInteractor
        every { poiInteractor.activeCategory } returns Observable.just(mockk<PoiCategory>())
        every { poiInteractor.poiData } returns Observable.just(mockk<PoiData>())
        every { poiInteractor.poiState } returns Observable.just(mockk<PoiState>())

        // Create the ViewModel
        viewModel = PoiGuideViewModel(
            poiInteractor,
            globalData,
            locationInteractor,
            prefs
        )

        // Observe LiveData
        viewModel.userLocation.observeForever(userLocationObserver)
        viewModel.launchCategorySelection.observeForever(launchCategorySelectionObserver)
        viewModel.poiData.observeForever(poiDataObserver)
        viewModel.activeCategory.observeForever(activeCategoryObserver)
        viewModel.showDetails.observeForever(showDetailsObserver)
        viewModel.isFirstTime.observeForever(isFirstTimeObserver)
        viewModel.poiState.observeForever(poiStateObserver)
    }

    @AfterEach
    fun tearDown() {
        // Remove observers to prevent memory leaks
        viewModel.userLocation.removeObserver(userLocationObserver)
        viewModel.launchCategorySelection.removeObserver(launchCategorySelectionObserver)
        viewModel.poiData.removeObserver(poiDataObserver)
        viewModel.activeCategory.removeObserver(activeCategoryObserver)
        viewModel.showDetails.removeObserver(showDetailsObserver)
        viewModel.isFirstTime.removeObserver(isFirstTimeObserver)
        viewModel.poiState.removeObserver(poiStateObserver)

        // Clear RxJava schedulers
        RxAndroidPlugins.reset()

        clearAllMocks()
    }

    @Nested
    inner class Initialization {
        @Test
        fun `should subscribe to activeCategory and poiData on init`() {
            // Verify subscriptions were made in init block
            verify { poiInteractor.activeCategory }
            verify { poiInteractor.poiData }

            // Verify observers received the initial values
            verify { activeCategoryObserver.onChanged(any()) }
            verify { poiDataObserver.onChanged(any()) }
        }
    }

    @Nested
    inner class LocationTests {
        @Test
        fun `onLocationPermissionAvailable should request location`() {
            // Given
            val mockLocation = LatLng(52.520008, 13.404954)
            every { locationInteractor.getLocation() } returns Single.just(mockLocation)

            // When
            viewModel.onLocationPermissionAvailable()

            // Then
            verify { locationInteractor.getLocation() }
            verify { userLocationObserver.onChanged(mockLocation) }
        }
    }

    @Nested
    inner class MarkerInteractionTests {
        @Test
        fun `onMarkerClick should post POI to showDetails when marker position matches`() {
            // Given
            val position = LatLng(52.52, 13.40)
            val mockPoi = mockk<PointOfInterest>()
            val mockPoiData = mockk<PoiData>()
            val mockItems = listOf(mockPoi)

            every { mockPoi.latitude } returns 52.52
            every { mockPoi.longitude } returns 13.40
            every { mockPoiData.items } returns mockItems

            // Set the current POI data
            val poiDataField = PoiGuideViewModel::class.java.getDeclaredField("_poiData")
            poiDataField.isAccessible = true
            (poiDataField.get(viewModel) as MutableLiveData<PoiData>).value = mockPoiData

            // When
            viewModel.onMarkerClick(position)

            // Then
            verify { showDetailsObserver.onChanged(mockPoi) }
        }

        @Test
        fun `onMarkerClick should not post to showDetails when no matching POI found`() {
            // Given
            val position = LatLng(52.52, 13.40)
            val mockPoi = mockk<PointOfInterest>()

            val mockPoiData = mockk<PoiData>()
            val mockItems = listOf(mockPoi)

            every { mockPoi.latitude } returns 51.51
            every { mockPoi.longitude } returns 12.39
            every { mockPoiData.items } returns mockItems

            // Set the current POI data
            val poiDataField = PoiGuideViewModel::class.java.getDeclaredField("_poiData")
            poiDataField.isAccessible = true
            (poiDataField.get(viewModel) as MutableLiveData<PoiData>).value = mockPoiData

            // When
            viewModel.onMarkerClick(position)

            // Then
            verify(exactly = 0) { showDetailsObserver.onChanged(any()) }
        }
    }

    @Nested
    inner class CategorySelectionTests {
        @Test
        fun `onRequestPermission should update isFirstTime based on preferences`() {
            // Given
            every { prefs.getPoiCategory("Berlin") } returns null

            // When
            viewModel.onRequestPermission()

            // Then
            verify { isFirstTimeObserver.onChanged(true) }
        }

        @Test
        fun `onRequestPermission should set isFirstTime to false when category exists`() {
            // Given
            val mockCategory = mockk<PoiCategory>()
            every { prefs.getPoiCategory("Berlin") } returns mockCategory

            // When
            viewModel.onRequestPermission()

            // Then
            verify { isFirstTimeObserver.onChanged(false) }
        }

        @Test
        fun `onCategorySelectionRequested should post stored category to launchCategorySelection`() {
            // Given
            val mockCategory = mockk<PoiCategory>()
            every { prefs.getPoiCategory("Berlin") } returns mockCategory

            // When
            viewModel.onCategorySelectionRequested()

            // Then
            verify { launchCategorySelectionObserver.onChanged(mockCategory) }
        }

        @Test
        fun `onCategorySelectionRequested should post null when no category stored`() {
            // Given
            every { prefs.getPoiCategory("Berlin") } returns null

            // When
            viewModel.onCategorySelectionRequested()

            // Then
            verify { launchCategorySelectionObserver.onChanged(null) }
        }
    }

    @Nested
    inner class ServiceTests {
        @Test
        fun `onServiceReady should load POIs when category exists and is different`() {
            // Given
            val mockCategory = mockk<PoiCategory>()
            every { prefs.getPoiCategory("Berlin") } returns mockCategory
            every { poiInteractor.selectedCategory } returns mockk()
            every { poiInteractor.getPois(mockCategory, true) } returns Completable.complete()

            // When
            viewModel.onServiceReady()

            // Then
            verify { activeCategoryObserver.onChanged(mockCategory) }
            verify { poiInteractor.getPois(mockCategory, true) }
        }

        @Test
        fun `onServiceReady should load POIs when refresh is required`() {
            // Given
            val mockCategory = mockk<PoiCategory>()
            every { prefs.getPoiCategory("Berlin") } returns mockCategory
            every { poiInteractor.selectedCategory } returns mockCategory
            every { poiInteractor.getPois(mockCategory, true) } returns Completable.complete()

            // When
            viewModel.onServiceReady(isRefreshRequired = true)

            // Then
            verify { activeCategoryObserver.onChanged(mockCategory) }
            verify { poiInteractor.getPois(mockCategory, true) }
        }

        @Test
        fun `onServiceReady should skip loading when category is the same and refresh not required`() {
            // Given
            val mockCategory = mockk<PoiCategory>()
            every { prefs.getPoiCategory("Berlin") } returns mockCategory
            every { poiInteractor.selectedCategory } returns mockCategory

            // When
            viewModel.onServiceReady(isRefreshRequired = false)

            // Then
            verify(exactly = 0) { poiInteractor.getPois(any(), any()) }
        }

        @Test
        fun `onServiceReady should launch category selection when no stored category`() {
            // Given
            every { prefs.getPoiCategory("Berlin") } returns null

            // When
            viewModel.onServiceReady()

            // Then
            verify { launchCategorySelectionObserver.onChanged(null) }
            verify(exactly = 0) { poiInteractor.getPois(any(), any()) }
        }

        @Test
        fun `onServiceReady should handle NoConnectionException`() {
            // Given
            val mockCategory = mockk<PoiCategory>()
            every { prefs.getPoiCategory("Berlin") } returns mockCategory
            every { poiInteractor.selectedCategory } returns mockk()
            every { poiInteractor.getPois(mockCategory, true) } returns Completable.error(NoConnectionException())

            // When mocking private methods and fields
            val showRetryDialogField = NetworkingViewModel::class.java.getDeclaredField("_showRetryDialog")
            showRetryDialogField.isAccessible = true
            val showRetryDialog = mockk<SingleLiveEvent<Unit>>(relaxed = true)
            showRetryDialogField.set(viewModel, showRetryDialog)

            // When
            viewModel.onServiceReady()

            // Then
            verify { showRetryDialog.call() }
        }

        @Test
        fun `onServiceReady should handle other exceptions`() {
            // Given
            val mockCategory = mockk<PoiCategory>()
            every { prefs.getPoiCategory("Berlin") } returns mockCategory
            every { poiInteractor.selectedCategory } returns mockk()
            every {
                poiInteractor.getPois(
                    mockCategory,
                    true
                )
            } returns Completable.error(RuntimeException("Test error"))

            // When mocking private fields
            val technicalErrorField = NetworkingViewModel::class.java.getDeclaredField("_technicalError")
            technicalErrorField.isAccessible = true
            viewModel.onServiceReady()
            print(PoiState.ERROR)
        }
    }
}
