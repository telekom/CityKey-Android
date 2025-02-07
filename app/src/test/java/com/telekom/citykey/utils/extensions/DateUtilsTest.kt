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
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.utils.extensions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Date

class DateUtilsTest {
    @Test
    fun `isDateInFuture check for today's date`() {
        val expectedDate = Date()
        val isDateInFuture = expectedDate.isInFuture
        Assertions.assertEquals(false, isDateInFuture)
    }

    @Test
    fun `isDateInFuture1check for past date`() {
        val expectedDate = Date(1627776000000L) // 1st August 2021, 00:00:00 GMT
        val isDateInFuture = expectedDate.isInFuture
        Assertions.assertEquals(false, isDateInFuture)
    }
    @Test
    fun `isDateInFuture1check for tomorrow's date`() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()  // Set current date
        calendar.add(Calendar.DAY_OF_YEAR, 1)  // Add one day
        val futureDate= calendar.time
        val isDateInFuture = futureDate.isInFuture
        Assertions.assertEquals(true, isDateInFuture)
    }
}