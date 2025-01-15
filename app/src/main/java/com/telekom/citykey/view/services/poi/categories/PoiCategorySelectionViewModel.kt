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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.services.poi.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.poi.POIInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.poi.PoiCategory
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
