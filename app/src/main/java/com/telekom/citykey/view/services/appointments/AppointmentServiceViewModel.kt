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

package com.telekom.citykey.view.services.appointments

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.view.BaseViewModel
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.BackpressureStrategy

class AppointmentServiceViewModel(
    private val cityInteractor: CityInteractor,
    private val inAppNotificationsInteractor: InAppNotificationsInteractor,
) : BaseViewModel() {

    val cityColor: LiveData<Int>
        get() = cityInteractor.city
            .map { Color.parseColor(it.cityColor) }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    val updates: LiveData<Int>
        get() = inAppNotificationsInteractor.badgesCounter
            .map { it[R.id.services_graph]?.get(ServicesFunctions.TERMINE) ?: 0 }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
}
