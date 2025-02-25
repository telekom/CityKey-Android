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

package com.telekom.citykey.view.home.news

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.content.CityContent
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class NewsViewModelTest {

    private lateinit var newsViewModel: NewsViewModel
    private val newsInteractor: NewsInteractor = mockk(relaxed = true)

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should post success state content to LiveData when interactor emits success state`() {
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
        val mockNewsContent = listOf(newsItem1, newsItem2)

        every { newsInteractor.newsObservable } returns Observable.just(NewsState.Success(mockNewsContent))

        newsViewModel = NewsViewModel(newsInteractor)

        assertEquals(true, newsViewModel.news.value.isNullOrEmpty().not())
        assertEquals(mockNewsContent, newsViewModel.news.value)

    }

    @Test
    fun `should not post error state content to LiveData when interactor emits non-success state`() {
        val errorState = NewsState.Error

        every { newsInteractor.newsObservable } returns Observable.just(errorState)

        newsViewModel = NewsViewModel(newsInteractor)

        assertEquals(true, newsViewModel.news.value.isNullOrEmpty())
    }

    @Test
    fun `should not post error state content to LiveData when interactor emits Loading state`() {
        val errorState = NewsState.Loading

        every { newsInteractor.newsObservable } returns Observable.just(errorState)

        newsViewModel = NewsViewModel(newsInteractor)
        assertEquals(true, newsViewModel.news.value.isNullOrEmpty())

    }

    @Test
    fun `should not post error state content to LiveData when interactor emits ActionError state`() {
        val errorState = NewsState.ActionError

        every { newsInteractor.newsObservable } returns Observable.just(errorState)

        newsViewModel = NewsViewModel(newsInteractor)
        assertEquals(true, newsViewModel.news.value.isNullOrEmpty())

    }
}
