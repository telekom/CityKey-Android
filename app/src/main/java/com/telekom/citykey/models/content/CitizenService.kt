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

package com.telekom.citykey.models.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CitizenService(
    val serviceId: Int,
    val service: String,
    val image: String,
    @SerializedName("header_image") val headerImage: String?,
    val icon: String,
    val restricted: Boolean,
    val residence: Boolean,
    val description: String,
    val serviceType: String?,
    val isNew: Boolean,
    val templateId: Int?,
    var loginLocked: Boolean = false,
    var cityLocked: String?,
    var displayFavoredIcon: Boolean = false,
    var favored: Boolean = false,
    val function: String?,
    val rank: Int?,
    var helpLinkTitle: String? = null,
    val serviceParams: Map<String, String>?,
    val serviceAction: List<ServiceAction>?,
    var category: String? = null
) : Parcelable

@Parcelize
class ServiceAction(
    val androidUri: String,
    val action: Int?,
    val visibleText: String,
    val type: Int,
    val buttonDesign: Int,
    val actionOrder: Int,
    val actionType: String?
) : Parcelable
