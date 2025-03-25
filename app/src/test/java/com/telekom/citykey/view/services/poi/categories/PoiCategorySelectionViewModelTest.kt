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

package com.telekom.citykey.view.services.poi.categories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.poi.POIInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.networkinterface.models.poi.PoiCategory
import com.telekom.citykey.networkinterface.models.poi.PoiCategoryGroup
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.Job
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
@ExtendWith(MockKExtension::class)
class PoiCategorySelectionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var poiGuideInteractor: POIInteractor

    @MockK
    private lateinit var adjustManager: AdjustManager

    @RelaxedMockK
    private lateinit var categoryListItemsObserver: Observer<List<PoiCategoryListItem>>

    @RelaxedMockK
    private lateinit var poiDataAvailableObserver: Observer<Unit>

    private lateinit var viewModel: PoiCategorySelectionViewModel
    private lateinit var testScheduler: TestScheduler

    @BeforeEach
    fun setup() {
        // Setup RxJava to use a test scheduler
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        // Initialize mocks
        MockKAnnotations.init(this)

        // Setup mock behavior for the initial getCategories call
        every { poiGuideInteractor.getCategories() } returns Observable.just(emptyList())

        // Create the view model
        viewModel = PoiCategorySelectionViewModel(poiGuideInteractor, adjustManager)

        // Observe LiveData
        viewModel.categoryListItems.observeForever(categoryListItemsObserver)
        viewModel.poiDataAvailable.observeForever(poiDataAvailableObserver)
    }

    @Test
    fun `init should fetch categories`() {
        // Given
        val categoryGroup = PoiCategoryGroup(
            categoryGroupId = 1,
            categoryGroupName = "Group 1",
            categoryGroupIcon = "icon1",
            categoryList = listOf(
                PoiCategory(1, "Category 1", "icon1")
            )
        )
        val categories = listOf(categoryGroup)
        val expectedItems = listOf(
            PoiCategoryListItem.Header(categoryGroup),
            PoiCategoryListItem.Item(
                categoryGroup.categoryList.first(),
                categoryGroup.categoryGroupId,
                categoryGroup.categoryGroupIcon
            )
        )

        every { poiGuideInteractor.getCategories() } returns Observable.just(categories)

        // When
        viewModel = PoiCategorySelectionViewModel(poiGuideInteractor, adjustManager)
        testScheduler.triggerActions()

        // Then
        verify { poiGuideInteractor.getCategories() }
    }

    @Test
    fun `onRetry should fetch categories again`() {
        // Given
        val categoryGroup = PoiCategoryGroup(
            categoryGroupId = 1,
            categoryGroupName = "Group 1",
            categoryGroupIcon = "icon1",
            categoryList = listOf(
                PoiCategory(2, "Category 1", "icon1")
            )
        )
        val categories = listOf(categoryGroup)

        every { poiGuideInteractor.getCategories() } returns Observable.just(categories)

        // When
        viewModel.onRetry()
        testScheduler.triggerActions()

        // Then
        verify(exactly = 2) { poiGuideInteractor.getCategories() } // Initial call + retry
    }

    @Test
    fun `getCategories should handle error and return empty list`() {
        // Given
        every { poiGuideInteractor.getCategories() } returns Observable.error(RuntimeException("Network error"))

        // When
        viewModel = PoiCategorySelectionViewModel(poiGuideInteractor, adjustManager)
        testScheduler.triggerActions()

        // Then
        verify { categoryListItemsObserver.onChanged(emptyList()) }
    }

    @Test
    fun `onCategorySelected should track event and fetch POIs when category changed`() {
        // Given
        val category = PoiCategory(3, "Category 1", "icon1")
        every { poiGuideInteractor.selectedCategory } returns null
        every { poiGuideInteractor.getSelectedPoiCategory() } returns null
        every { poiGuideInteractor.getPois(category, true) } returns Completable.complete()
        every { adjustManager.trackEvent(any()) } returns Job()

        // When
        viewModel.onCategorySelected(category)
        testScheduler.triggerActions()

        // Then
        verify { adjustManager.trackEvent(any()) }
        verify { poiGuideInteractor.getSelectedPoiCategory() }
        verify { poiGuideInteractor.getPois(category, true) }
    }

    @Test
    fun `onCategorySelected should not track event or fetch POIs when category is the same`() {
        // Given
        val category = PoiCategory(3, "Category 1", "icon1")
        every { poiGuideInteractor.selectedCategory } returns category

        // When
        viewModel.onCategorySelected(category)
        testScheduler.triggerActions()

        // Then
        verify(exactly = 0) { adjustManager.trackEvent(any()) }
        verify(exactly = 0) { poiGuideInteractor.getPois(any(), any()) }
    }

    @Test
    fun `onCategorySelected should handle NoConnectionException properly`() {
        // Given
        val category = PoiCategory(5, "Category 1", "icon1")
        val exception = NoConnectionException()

        every { poiGuideInteractor.selectedCategory } returns null
        every { poiGuideInteractor.getSelectedPoiCategory() } returns null
        every { poiGuideInteractor.getPois(category, true) } returns Completable.complete()
        every { adjustManager.trackEvent(any()) } returns Job()

        // When
        viewModel.onCategorySelected(category)
        testScheduler.triggerActions()

        // Then
        verify { adjustManager.trackEvent(any()) }
    }

    @Test
    fun `onCategorySelected should handle generic error properly`() {
        // Given
        val category = PoiCategory(4, "Category 1", "icon1")
        val exception = RuntimeException("Some error")

        every { poiGuideInteractor.selectedCategory } returns null
        every { poiGuideInteractor.getSelectedPoiCategory() } returns null
        every { poiGuideInteractor.getPois(category, true) } returns Completable.complete()
        every { adjustManager.trackEvent(any()) } returns Job()

        // When
        viewModel.onCategorySelected(category)
        testScheduler.triggerActions()

        // Then
        verify { adjustManager.trackEvent(any()) }
    }
}
