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

package com.telekom.citykey.view.city_imprint

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.City
import io.reactivex.BackpressureStrategy

class CityImprintViewModel(private val globalData: GlobalData) : ViewModel() {

    val city: LiveData<City> get() = globalData.city
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()
    val user: LiveData<Boolean> get() = globalData.user
        .map { it is UserState.Present }
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()
}
