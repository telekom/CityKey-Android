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

package com.telekom.citykey.domain.track

object AnalyticsParameterKey {

    const val adjustDeviceId = "adjust_device_id"
    const val moengageCustomerId = "moengage_customer_id"

    const val userStatus = "user_status"
    const val userZipCode = "user_zipcode"

    const val registeredCityName = "registered_city_name"
    const val registeredCityId = "registered_city_id"

    const val selectedCityName = "city_selected"
    const val selectedCityId = "city_id"

    const val userYearOfBirth = "user_yob"

    const val eventId = "event_id"
    const val eventEngagementOption = "engagement_option"

    const val digitalAdminCategory = "category_of_services"
    const val digitalAdminSubcategory = "subcategory_of_services"

    const val serviceType = "service_type"
    const val serviceActionType = "service_action_type"

}

object AnalyticsParameterValue {

    const val empty = ""
    const val loggedIn = "Logged In"
    const val notLoggedIn = "Not Logged In"

}
