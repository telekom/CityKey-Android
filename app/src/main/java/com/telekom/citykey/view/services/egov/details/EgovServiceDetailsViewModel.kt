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

package com.telekom.citykey.view.services.egov.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.egov.EgovInterractor
import com.telekom.citykey.domain.services.egov.EgovState
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.track.AnalyticsParameterKey
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class EgovServiceDetailsViewModel(
    private val egovInterractor: EgovInterractor,
    private val adjustManager: AdjustManager
) : NetworkingViewModel() {

    val state: LiveData<EgovState> get() = _state
    private val _state: MutableLiveData<EgovState> = MutableLiveData()

    init {
        loadEgovItems()

        launch {
            egovInterractor.egovStateObservable
                .subscribe(_state::postValue)
        }
    }

    fun onRetryClicked() {
        loadEgovItems()
    }

    private fun loadEgovItems() {
        launch {
            egovInterractor.loadEgovItems()
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "egov")
                .subscribe({}, this::onError)
        }
    }

    fun clickSubCategory(category: String, subcategory: String) {
        adjustManager.trackEvent(
            R.string.digital_adm_subcategories,
            mapOf(
                AnalyticsParameterKey.digitalAdminCategory to category,
                AnalyticsParameterKey.digitalAdminSubcategory to subcategory
            )
        )
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> _showRetryDialog.call()
            else -> _technicalError.value = Unit
        }
    }
}
