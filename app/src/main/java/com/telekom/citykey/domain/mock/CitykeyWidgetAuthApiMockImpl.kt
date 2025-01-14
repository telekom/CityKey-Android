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

package com.telekom.citykey.domain.mock

import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.contracts.CitykeyWidgetAuthApi
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.models.waste_calendar.GetSelectedWastePickupsResponse
import com.telekom.citykey.models.waste_calendar.WasteCalendarResponse

private const val GET_WASTE_CALENDAR_PICKUP_IDS = "get_selected_waste_pickups"
private const val GET_SELECTED_WASTE_PICKUPS_DATA = "get_waste_calendar"

class CitykeyWidgetAuthApiMockImpl(
    private val assetResponseMocker: AssetResponseMocker
) : CitykeyWidgetAuthApi {

    override suspend fun getWasteCalendarPickupIds(
        cityId: Int,
        actionName: String
    ): OscaResponse<GetSelectedWastePickupsResponse> = assetResponseMocker.getOscaResponseOf(
        GET_WASTE_CALENDAR_PICKUP_IDS
    )

    override suspend fun getSelectedWastePickupsData(
        wasteCalendarRequest: WasteCalendarRequest,
        cityId: Int,
        actionName: String
    ): OscaResponse<WasteCalendarResponse> = assetResponseMocker.getOscaResponseOf(
        GET_SELECTED_WASTE_PICKUPS_DATA
    )
}
