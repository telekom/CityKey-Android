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

package com.telekom.citykey.view.auth_webview

import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.ausweiss_app.IdentConst
import com.telekom.citykey.domain.ausweiss_app.IdentInteractor
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy

class AuthBottomSheetDialogViewModel(
    private val identInteractor: IdentInteractor,
    private val url: String
) : BaseViewModel() {

    val newState = identInteractor.state
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()

    fun onFeatureReady() {
        identInteractor.startIdentification(url)
    }

    fun onAccessAccepted() {
        identInteractor.sendCMD(IdentConst.CMD_ACCEPT)
    }

    fun onSubmitPin(pin: String) {
        identInteractor.setPin(pin)
    }

    fun onSubmitCan(can: String) {
        identInteractor.setCan(can)
    }

    fun onSubmitPuk(puk: String) {
        identInteractor.setPuk(puk)
    }

    override fun onCleared() {
        identInteractor.unBind()
        super.onCleared()
    }
}
