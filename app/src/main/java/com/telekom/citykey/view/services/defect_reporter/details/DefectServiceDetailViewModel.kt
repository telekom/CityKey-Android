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

package com.telekom.citykey.view.services.defect_reporter.details

import androidx.lifecycle.*
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.defect_reporter.DefectReporterInteractor
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class DefectServiceDetailViewModel(
    private val defectReporterInteractor: DefectReporterInteractor
) : NetworkingViewModel() {
    val defectCategoryAvailable: LiveData<Unit> get() = _defectCategoryAvailable
    private val _defectCategoryAvailable: MutableLiveData<Unit> = SingleLiveEvent()

    fun onOpenDefectReporterClicked() {
        launch {
            defectReporterInteractor.loadCategories()
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "defectCat")
                .subscribe({ _defectCategoryAvailable.value = Unit }, this::onError)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> {
                showRetry()
            }
            else -> {
                _technicalError.value = Unit
            }
        }
    }
}
