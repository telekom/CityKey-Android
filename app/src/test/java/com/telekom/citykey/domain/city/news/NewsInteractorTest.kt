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

package com.telekom.citykey.domain.city.news

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.networkinterface.models.error.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.networkinterface.models.content.City
import com.telekom.citykey.networkinterface.models.content.CityConfig
import com.telekom.citykey.networkinterface.models.content.CityContent
import com.telekom.citykey.networkinterface.models.error.OscaError
import com.telekom.citykey.networkinterface.models.error.OscaErrorResponse
import com.telekom.citykey.utils.DateUtil
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class NewsInteractorTest {
    private val cityRepository: CityRepository = mockk(relaxed = true)
    val globalData: GlobalData = mockk(relaxed = true)
    private lateinit var newsInteractor: NewsInteractor
    private lateinit var testScheduler: TestScheduler
    private val newsItem1 =
        CityContent(
            1,
            Date(1000L),
            1,
            "ContentDetailsText1",
            "ContentTeaser1",
            "ContentSubtitle1",
            "ContentSource1",
            "ContentImage1",
            "ContentType1",
            "ContentCategory1",
            "ImageCredit1",
            "Thumbnail1",
            "ThumbnailCredit1",
            true
        )
    private val newsItem2 =
        CityContent(
            2,
            Date(2000L),
            2,
            "ContentDetailsText2",
            "ContentTeaser2",
            "ContentSubtitle2",
            "ContentSource2",
            "ContentImage2",
            "ContentType2",
            "ContentCategory2",
            "ImageCredit2",
            "Thumbnail2",
            "ThumbnailCredit2",
            true
        )
    private val newsItem3 = CityContent(
        3,
        Date(3000L),
        3,
        "ContentDetailsText3",
        "ContentTeaser3",
        "ContentSubtitle3",
        "ContentSource3",
        "ContentImage3",
        "ContentType3",
        "ContentCategory3",
        "ImageCredit3",
        "Thumbnail3",
        "ThumbnailCredit3",
        true
    )

    @BeforeEach
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { globalData.city } returns Observable.just(
            City(
                cityId = 1,
                cityConfig = CityConfig(stickyNewsCount = 4)
            )
        )
        testScheduler = TestScheduler()
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        newsInteractor = NewsInteractor(cityRepository, globalData)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `init block should trigger observeCity and update newsObservable with Loading state`() {
        // Verify that the _newsSubject in NewsInteractor was updated to Loading when a new city is observed
        val newsObserver = mockk<BehaviorSubject<NewsState>>(relaxed = true)
        mockkConstructor(BehaviorSubject::class)
        every { anyConstructed<BehaviorSubject<NewsState>>().onNext(any()) } answers {
            newsObserver.onNext(NewsState.Loading)
        }

        // Trigger the init block by creating an instance of NewsInteractor
        newsInteractor = NewsInteractor(cityRepository, globalData)

        verify { newsObserver.onNext(NewsState.Loading) } // Verify that Loading state was posted
    }

    @Test
    fun `init block should handle Success response and update newsObservable`() {
        // Given
        val mockCityContent = listOf(mockk<CityContent>(relaxed = true))
        val successState = NewsState.Success(mockCityContent)

        // Mock CityRepository to return a success state when getNews is called
        every { cityRepository.getNews(1) } returns Maybe.just(successState)

        // Mock _newsSubject to verify its state updates
        val newsSubject = mockk<BehaviorSubject<NewsState>>(relaxed = true)
        mockkConstructor(BehaviorSubject::class)
        every { anyConstructed<BehaviorSubject<NewsState>>().onNext(any()) } answers {
            newsSubject.onNext(it.invocation.args[0] as NewsState)
        }

        // When
        newsInteractor = NewsInteractor(cityRepository, globalData) // This triggers the init block

        // Then
        verify { newsSubject.onNext(successState) } // Verify that the success state was posted to _newsSubject
    }

    @Test
    fun `init block should handle Success response and update newsObservable with correct content`() {
        // Given
        val cityContentList = listOf(newsItem1, newsItem2, newsItem3)

        val successState = NewsState.Success(cityContentList)

        // Mock CityRepository to return a success state when getNews is called
        every { cityRepository.getNews(1) } returns Maybe.just(successState)

        // Mock _newsSubject to verify its state updates
        val newsSubject = spyk(BehaviorSubject.create<NewsState>())
        mockkConstructor(BehaviorSubject::class)
        every { anyConstructed<BehaviorSubject<NewsState>>().onNext(any()) } answers {
            newsSubject.onNext(it.invocation.args[0] as NewsState)
        }

        // When
        newsInteractor = NewsInteractor(cityRepository, globalData) // This triggers the init block

        // Then
        verify { newsSubject.onNext(successState) } // Verify that the success state was posted to _newsSubject

        // Assert that the content received is the same as the mock data
        assert(newsSubject.value is NewsState.Success)
        assert((newsSubject.value as NewsState.Success).content == cityContentList)

    }


    @Test
    fun `updateWidgetDone resets shouldUpdateWidget`() {
        newsInteractor.updateWidgetDone()
        Assertions.assertFalse(newsInteractor.shouldUpdateWidget)
    }

    @Test
    fun `mapContent returns error when news state is error`() {
        val stateItem: NewsState = NewsState.Error
        Assertions.assertEquals(NewsState.Error, newsInteractor.mapContent(stateItem))
    }

    @Test
    fun `mapContent returns error when news state is ActionError`() {
        val stateItem: NewsState = NewsState.ActionError
        Assertions.assertEquals(NewsState.ActionError, newsInteractor.mapContent(stateItem))
    }

    @Test
    fun `mapContent returns error when news state is Loading`() {
        val stateItem: NewsState = NewsState.Loading
        Assertions.assertEquals(NewsState.Loading, newsInteractor.mapContent(stateItem))
    }

    @Test
    fun `mapContent returns list when news state is success and number of items in list is less than size of stickyNewsCount`() {

        val cityContentList = listOf(newsItem1, newsItem2)
        val response = NewsState.Success(cityContentList)
        every { cityRepository.getNews(1) } returns Maybe.just(response)

        val stateItem = NewsState.Success(cityContentList)

        val a: NewsState = newsInteractor.mapContent(stateItem)
        Assertions.assertTrue(a is NewsState.Success)
        val actualSize = a as NewsState.Success

        Assertions.assertEquals(2, actualSize.content.size)
    }

    @Test
    fun `mapContent returns list when news state is success and number of items in list is more than size of stickyNewsCount`() {
        val newsItem4 =
            CityContent(
                4, DateUtil.stringToDate("2024-08-02", DateUtil.FORMAT_DD_MMMM_YYYY), 1, "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                false
            )
        val newsItem5 =
            CityContent(
                5,
                DateUtil.stringToDate("2024-08-02", DateUtil.FORMAT_DD_MMMM_YYYY),
                1,
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                "abc",
                false
            )

        val cityContentList = listOf(newsItem1, newsItem2, newsItem3, newsItem4, newsItem5)
        val response = NewsState.Success(cityContentList)
        every { cityRepository.getNews(1) } returns Maybe.just(response)

        val stateItem = NewsState.Success(cityContentList)

        val a: NewsState = newsInteractor.mapContent(stateItem)
        Assertions.assertTrue(a is NewsState.Success)
        val actualSize = a as NewsState.Success

        Assertions.assertEquals(4, actualSize.content.size)
    }

    @Test
    fun `test onError with error code is ACTION_NOT_AVAILABLE`() {
        // Arrange
        val exception = NetworkException(
            1,
            OscaErrorResponse(
                listOf(
                    OscaError(
                        "error",
                        ErrorCodes.ACTION_NOT_AVAILABLE
                    )
                )
            ),
            "",
            NetworkException(1, "", "", Throwable())
        )

        every { cityRepository.getNews(any()) } returns Maybe.error(exception)
        every { globalData.city } returns Observable.just(City(cityId = 1, cityConfig = null))

        newsInteractor = NewsInteractor(cityRepository, globalData) // Recreate to trigger init block
        val testObserver = newsInteractor.newsObservable.test()

        // Assert
        testObserver.assertValue(NewsState.ActionError)
    }

    @Test
    fun `test onError with error code other than ACTION_NOT_AVAILABLE error codes`() {

        val exception = NetworkException(
            1,
            OscaErrorResponse(
                listOf(
                    OscaError(
                        "error",
                        ErrorCodes.CHANGE_POSTAL_CODE_VALIDATION_ERROR
                    )
                )
            ),
            "",
            NetworkException(1, "", "", Throwable())
        )

        every { cityRepository.getNews(any()) } returns Maybe.error(exception)
        every { globalData.city } returns Observable.just(City(cityId = 1, cityConfig = null))

        newsInteractor = NewsInteractor(cityRepository, globalData) // Recreate to trigger init block
        val testObserver = newsInteractor.newsObservable.test()

        // Assert
        testObserver.assertValue(NewsState.Error)
    }
}
