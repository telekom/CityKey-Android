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

package com.telekom.citykey.view

import androidx.lifecycle.LiveData
import com.telekom.citykey.utils.SingleLiveEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.reactivestreams.Subscription

abstract class NetworkingViewModel : BaseViewModel() {

    private val _retryDispatcher: PublishSubject<String> = PublishSubject.create()
    protected val retryDispatcher: Flowable<String> get() = _retryDispatcher.toFlowable(BackpressureStrategy.DROP)
    protected val pendingRetries = mutableMapOf<String, Subscription?>()

    val showRetryDialog: LiveData<String?> get() = _showRetryDialog
    val technicalError: LiveData<Unit> get() = _technicalError
    @Suppress("PropertyName", "VariableNaming")
    protected val _showRetryDialog: SingleLiveEvent<String?> = SingleLiveEvent()
    @Suppress("PropertyName", "VariableNaming")
    protected val _technicalError: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onRetryRequired() {
        pendingRetries.forEach { entry -> _retryDispatcher.onNext(entry.key) }
    }

    protected fun showRetry() {
        _showRetryDialog.call()
    }

    fun onRetryCanceled() {
        pendingRetries.forEach { entry -> entry.value?.cancel() }
        pendingRetries.clear()
    }

    override fun onCleared() {
        super.onCleared()
        pendingRetries.forEach { entry -> entry.value?.cancel() }
        pendingRetries.clear()
    }
}
