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

import android.annotation.SuppressLint
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.content.City
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class NewsInteractor(private val cityRepository: CityRepository, private val globalData: GlobalData) {

    private var refreshDisposable: Disposable? = null
    private var lastCityId = -1
    private var stickyNewsCount = 0

    private val _newsSubject: BehaviorSubject<NewsState> = BehaviorSubject.create()
    val newsObservable: Observable<NewsState> = _newsSubject.hide()

    private var _shouldUpdateWidget = false
    val shouldUpdateWidget get() = _shouldUpdateWidget

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        globalData.city.subscribe {
            stickyNewsCount = it.cityConfig?.stickyNewsCount ?: 0
            if (lastCityId != it.cityId) {
                lastCityId = it.cityId
                _newsSubject.onNext(NewsState.Loading)
            }
            refreshNews(it)
            _shouldUpdateWidget = true
        }
    }

    private fun refreshNews(city: City) {
        refreshDisposable?.dispose()
        refreshDisposable = cityRepository.getNews(city.cityId)
            .subscribe(_newsSubject::onNext, this::onError)
    }

    fun updateWidgetDone() {
        _shouldUpdateWidget = false
    }

    fun mapContent(stateItem: NewsState): NewsState {
        if (stateItem !is NewsState.Success)
            return stateItem

        val allNews = stateItem.content
        var stickyNews = allNews.filter { it.sticky }
            .toMutableList()

        // Fill sticky news if they are less than expected
        if (stickyNews.size < stickyNewsCount) {
            allNews.takeWhile {
                if (!stickyNews.contains(it)) stickyNews.add(it)
                stickyNews.size < stickyNewsCount
            }
        }

        // If the number of news is bigger than expected we take first ones
        stickyNews = stickyNews.take(stickyNewsCount).toMutableList()
        return NewsState.Success(stickyNews)
    }

    private fun onError(throwable: Throwable) {
        if (_newsSubject.value !is NewsState.Success)
            when (throwable) {
                is NetworkException -> {
                    (throwable.error as OscaErrorResponse).errors.forEach {
                        when (it.errorCode) {
                            ErrorCodes.ACTION_NOT_AVAILABLE -> {
                                _newsSubject.onNext(NewsState.ActionError)
                            }
                            else -> {
                                _newsSubject.onNext(NewsState.Error)
                            }
                        }
                    }
                }
            }
    }
}
