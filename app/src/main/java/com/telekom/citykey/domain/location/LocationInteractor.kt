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

package com.telekom.citykey.domain.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import io.reactivex.Single

class LocationInteractor(private val client: FusedLocationProviderClient) {

    private val cancellationTokenSource = CancellationTokenSource()

    @SuppressLint("MissingPermission")
    fun getLocation(): Single<LatLng> = Single.create {
        client.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnCompleteListener { task ->
            if (it.isDisposed) return@addOnCompleteListener

            if (task.isSuccessful && task.result != null) {
                val result: Location = task.result
                it.onSuccess(LatLng(result.latitude, result.longitude))
            } else {
                it.onError(Exception())
            }
        }
    }

    fun cancelLocation() {
        cancellationTokenSource.cancel()
    }
}
