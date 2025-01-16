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

package com.telekom.citykey.view.services.service_detail_help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class ServiceDetailHelpViewModel(
    private val servicesInteractor: ServicesInteractor,
    private val serviceId: Int
) : NetworkingViewModel() {

    val info: LiveData<String?> get() = _info
    private val _info: MutableLiveData<String?> = MutableLiveData()

    init {
        getData()
    }

    fun onRetryClicked() {
        getData()
    }

    private fun getData() {
        launch {
            servicesInteractor.getInfo(serviceId)
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .map { it.helpText }
                .subscribe(_info::postValue, this::onError)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> {
                super.showRetry()
                _info.postValue(null)
            }
            else -> {
                _info.postValue(null)
                _technicalError.postValue(Unit)
            }
        }
    }
}
