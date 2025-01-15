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

package com.telekom.citykey.models.appointments

import android.os.Parcelable
import com.telekom.citykey.utils.extensions.isInPast
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Appointment(
    val apptId: String,
    var apptStatus: String,
    val attendee: List<Attendee>?,
    val contacts: Contacts,
    val documents: List<String>,
    val createdTime: Date,
    val endTime: Date,
    val location: Location,
    val notes: String,
    val reasons: List<Reason>?,
    val startTime: Date,
    val title: String,
    val uuid: String,
    val waitingNo: String,
    var isRead: Boolean
) : Parcelable {
    companion object {
        const val STATE_PENDING = "Reservierung"
        const val STATE_CONFIRMED = "Bestätigung"
        const val STATE_CANCELED = "Stornierung"
        const val STATE_REJECTED = "Ablehnung"
        const val STATE_UPDATED = "Änderung"
    }

    val isCancellable: Boolean get() = (apptStatus == STATE_CONFIRMED || apptStatus == STATE_UPDATED) && !endTime.isInPast
    val canBeDeleted: Boolean get() = apptStatus == STATE_REJECTED || apptStatus == STATE_CANCELED || endTime.isInPast
}
