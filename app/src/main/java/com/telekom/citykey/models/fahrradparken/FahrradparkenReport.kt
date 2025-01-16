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

package com.telekom.citykey.models.fahrradparken

import com.google.gson.annotations.SerializedName

data class FahrradparkenReport(
    @SerializedName("service_request_id") var serviceRequestId: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("lat") var lat: Double? = null,
    @SerializedName("long") var lng: Double? = null,
    @SerializedName("service_name") var serviceName: String? = null,
    @SerializedName("media_url") var mediaUrl: String? = null,
    @SerializedName("extended_attributes") var extendedAttributes: ExtendedAttributes? = null
)

data class ExtendedAttributes(
    @SerializedName("markaspot") var markASpot: MarkASpot? = null
)

data class MarkASpot(
    @SerializedName("status_descriptive_name") var statusDescriptiveName: String? = null,
    @SerializedName("status_hex") var statusHex: String? = null,
    @SerializedName("status_icon") var statusIcon: String? = null
)
