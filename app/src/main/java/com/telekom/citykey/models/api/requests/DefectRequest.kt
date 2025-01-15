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

package com.telekom.citykey.models.api.requests

import com.google.gson.annotations.SerializedName

class DefectRequest(
    val lastName: String,
    val wasteBinId: String,
    val firstName: String,
    @SerializedName("service_code") val serviceCode: String,
    val lat: String,
    val long: String,
    val email: String,
    val description: String,
    @SerializedName("media_url") var mediaUrl: String,
    @SerializedName("sub_service_code") var subServiceCode: String,
    var houseNumber: String = "",
    var location: String = "",
    var phoneNumber: String,
    var postalCode: String = "",
    var streetName: String = ""
)
