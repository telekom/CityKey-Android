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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.data.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.poi.POIInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.networkinterface.models.poi.PoiCategory
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class PoiCategorySelectionViewModel(
    private val poiGuideInteractor: POIInteractor,
    private val adjustManager: AdjustManager
) : NetworkingViewModel() {

    val categoryListItems: LiveData<List<PoiCategoryListItem>> get() = _categoryListItems
    private val _categoryListItems: MutableLiveData<List<PoiCategoryListItem>> = MutableLiveData()

    val poiDataAvailable: LiveData<Unit> get() = _poiDataAvailable
    private val _poiDataAvailable: SingleLiveEvent<Unit> = SingleLiveEvent()

    init {
        getCategories()
    }

    fun onRetry() {
        getCategories()
    }

    private fun getCategories() {
        launch {
            poiGuideInteractor.getCategories()
                .map {
                    val listItem = mutableListOf<PoiCategoryListItem>()

                    it.forEach { category ->
                        listItem.add(PoiCategoryListItem.Header(category))
                        listItem.addAll(
                            category.categoryList.map { listItem ->
                                PoiCategoryListItem.Item(
                                    listItem,
                                    category.categoryGroupId,
                                    category.categoryGroupIcon
                                )
                            }
                        )
                    }

                    listItem.toList()
                }
                .onErrorReturnItem(emptyList())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_categoryListItems::postValue, Timber::e)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showRetryDialog.call()
            else -> _technicalError.value = Unit
        }
    }

    fun onCategorySelected(category: PoiCategory) {
        if (category != poiGuideInteractor.selectedCategory) {
            adjustManager.trackEvent(R.string.change_poi_category)
            launch {
                val isInitialLoading = poiGuideInteractor.getSelectedPoiCategory() == null
                poiGuideInteractor.getPois(category, isInitialLoading)
                    .retryOnError(this::onError, retryDispatcher, pendingRetries, "POIs")
                    .subscribe({ _poiDataAvailable.call() }, this::onError)
            }
        } else {
            _poiDataAvailable.call()
        }
    }
}
