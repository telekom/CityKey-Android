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

data class CityConfig(
    val showFavouriteServices: Boolean = false,
    val showNewServices: Boolean = false,
    val showMostUsedServices: Boolean = false,
    val showCategories: Boolean = false,
    val showOurServices: Boolean = false,
    val showHomeDiscounts: Boolean = false,
    val showHomeOffers: Boolean = false,
    val showHomeTips: Boolean = false,
    val stickyNewsCount: Int = 0,
    val eventsCount: Int = 0,
    val yourEventsCount: Int = 0,
    val showServicesOption: Boolean = false
)
