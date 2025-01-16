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

package com.telekom.citykey.view.dialogs.dpn_updates

import androidx.lifecycle.LiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel

class DpnUpdatesViewModel(
    private val oscaRepository: OscaRepository,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    val newDpnAccepted: LiveData<Unit> get() = _newDpnAccepted

    private val _newDpnAccepted: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onChangesAccepted() {
        launch {
            oscaRepository.acceptDataSecurityChanges(true)
                .andThen(globalData.loadUser())
                .retryOnError(::onError, retryDispatcher, pendingRetries, "acceptDpn")
                .subscribe({ _newDpnAccepted.call() }, this::onError)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NoConnectionException -> showRetry()
            is InvalidRefreshTokenException -> globalData.logOutUser(throwable.reason)
            else -> _technicalError.call()
        }
    }
}
