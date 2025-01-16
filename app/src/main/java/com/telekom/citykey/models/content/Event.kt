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
import com.telekom.citykey.utils.extensions.isSameDayAs
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Event(
    val eventId: String? = null,
    val uid: Long,
    val link: String?,
    val title: String?,
    val subtitle: String?,
    val description: String?,
    val thumbnail: String?,
    val image: String?,
    val imageCredit: String?,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String?,
    val locationAddress: String?,
    val startDate: Date?,
    val endDate: Date?,
    val hasStartTime: Boolean = false,
    val hasEndTime: Boolean = false,
    val pdf: ArrayList<String>?,
    val cityEventCategories: List<EventCategory> = emptyList(),
    val status: String? = null
) : Parcelable {
    val isSingleDay get() = startDate.isSameDayAs(endDate)

    @IgnoredOnParcel
    var isFavored = false
    val isCancelled: Boolean get() = status.contentEquals("CANCELLED", ignoreCase = true)
    val isPostponed: Boolean get() = status.contentEquals("POSTPONED", ignoreCase = true)
    val isSoldOut: Boolean get() = status.contentEquals("SOLDOUT", ignoreCase = true)
}

@Parcelize
data class EventCategory(val id: Int?, val categoryName: String?) : Parcelable
