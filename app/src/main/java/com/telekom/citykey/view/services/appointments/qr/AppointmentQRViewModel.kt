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

package com.telekom.citykey.view.services.appointments.qr

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.utils.QRUtils
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AppointmentQRViewModel(
    private val globalData: GlobalData
) : BaseViewModel() {

    val qrBitmap: LiveData<Bitmap> get() = _qrBitmap
    val userLoggedOut: LiveData<Unit> get() = globalData.user
        .filter { it is UserState.Absent }
        .map { Unit }
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()

    private val _qrBitmap: MutableLiveData<Bitmap> = MutableLiveData()

    fun onViewCreated(uuid: String) {
        launch {
            Single.just(uuid)
                .subscribeOn(Schedulers.io())
                .map(QRUtils::generateQRBitmap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_qrBitmap::postValue, Timber::e)
        }
    }
}
