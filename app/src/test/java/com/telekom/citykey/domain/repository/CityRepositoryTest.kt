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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.domain.repository

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.content.CityContent
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class CityRepositoryTest {
    val api: SmartCityApi = mockk(relaxed = true)
    private val authApi: SmartCityAuthApi = mockk(relaxed = true)
    private lateinit var cityRepository: CityRepository
    private lateinit var testScheduler: TestScheduler
    private val cityId = 123

    @BeforeEach
    fun setup() {
        cityRepository = CityRepository(api, authApi)
        testScheduler = TestScheduler()

    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getNews should return Success state with sorted content on success`() {
        val newsItem1 =
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
        val newsItem2 =
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
        val newsItem3 = CityContent(
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
        val cityContentList = listOf(newsItem1, newsItem2, newsItem3)
        every { api.getCityContent(cityId = cityId) } returns Maybe.just(
            OscaResponse(
                listOf(
                    newsItem1,
                    newsItem2,
                    newsItem3
                )
            )
        )

        val testObserver = TestObserver<NewsState>()
        cityRepository.getNews(cityId = cityId).subscribe(testObserver)

        testScheduler.triggerActions()

        // Then
        testObserver.assertValue {
            it is NewsState.Success && it.content == cityContentList.sortedByDescending { it1 -> it1.contentCreationDate }
        }
        testObserver.assertComplete()
        testObserver.assertNoErrors()

        verify { api.getCityContent(cityId = cityId) }
    }

    @Test
    fun `getNews should return Error state on API error`() {
        // Given
        val error = Throwable("Network Error")
        every { api.getCityContent(cityId) } returns Maybe.error(error)

        // When
        val testObserver = TestObserver<NewsState>()
        cityRepository.getNews(cityId).subscribe(testObserver)

        // Advance the scheduler to process the Rx chain
        testScheduler.triggerActions()

        // Then
        testObserver.assertError(error)
        testObserver.assertNotComplete()

        verify { api.getCityContent(cityId) }
    }

    @Test
    fun `getNews should handle error response when NetworkException`() {
        val networkException = NetworkException(1, null, "", Throwable())
        every { api.getCityContent(cityId) } throws networkException
        assertThrows<NetworkException> {
            cityRepository.getNews(cityId)
        }
    }
}