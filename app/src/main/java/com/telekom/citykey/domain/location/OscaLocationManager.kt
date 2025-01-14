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

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class OscaLocationManager(private val geocoder: Geocoder) {

    fun getLatLngFromAddress(strAddress: String): Single<LatLng> {
        return Single.create<LatLng> {
            val address = geocoder.getFromLocationName(strAddress, 1)?.get(0)
            it.onSuccess(LatLng(address!!.latitude, address.longitude))
        }
            .subscribeOn(Schedulers.io())
    }

    fun getAddressFromLatLng(addressLat: Double, addressLong: Double): Maybe<Address> {
        return Maybe.create<Address> {
            val address = geocoder.getFromLocation(addressLat, addressLong, 1)?.get(0)
            if (address != null) {
                it.onSuccess(address)
            }
        }.subscribeOn(Schedulers.io())
    }
}
