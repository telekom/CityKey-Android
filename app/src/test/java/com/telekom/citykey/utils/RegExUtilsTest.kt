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

package com.telekom.citykey.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegExUtilsTest {

    @Test
    fun testAnchorTag() {
        val testString = "<a href=\"http://example.com\">Example</a>"
        val matcher = RegExUtils.anchorTag.matcher(testString)
        assertTrue(matcher.find())
    }

    @Test
    fun testAnchorTagHrefValue() {
        val testString = "<a href=\"http://example.com\">Example</a>"
        val matcher = RegExUtils.anchorTagHrefValue.matcher(testString)
        assertTrue(matcher.find())
    }

    @Test
    fun testEmailAddress() {
        val validEmails = listOf("test@example.com", "user.name+tag+sorting@example.com", "user@example.co.in")
        val invalidEmails = listOf("plainaddress", "@missingusername.com", "username@.com")

        validEmails.forEach { email ->
            val matcher = RegExUtils.emailAddress.matcher(email)
            assertTrue(matcher.matches(), "Expected valid email: $email")
        }

        invalidEmails.forEach { email ->
            val matcher = RegExUtils.emailAddress.matcher(email)
            assertFalse(matcher.matches(), "Expected invalid email: $email")
        }
    }

    @Test
    fun testWebUrl() {
        val validUrls = listOf("http://example.com", "https://example.com", "http://www.example.com")
        val invalidUrls = listOf("htt://example.com", "http:/example.com")

        validUrls.forEach { url ->
            val matcher = RegExUtils.webUrl.matcher(url)
            assertTrue(matcher.matches(), "Expected valid URL: $url")
        }

        invalidUrls.forEach { url ->
            val matcher = RegExUtils.webUrl.matcher(url)
            assertFalse(matcher.matches(), "Expected invalid URL: $url")
        }
    }

    @Test
    fun testPhoneNumber() {
        val validNumbers = listOf(
            "2055550125", "202 555 0125", "(202) 555-0125", "+111 (202) 555-0125",
            "636 856 789", "+111 636 856 789", "636 85 67 89", "+111 636 85 67 89",
            "02055 550 125"
        )
        val invalidNumbers = listOf(
            "12345", "555-0125", "202-555-012", "+1 (202) 555-0125x1234"
        )

        validNumbers.forEach { number ->
            val matcher = RegExUtils.phoneNumber.matcher(number)
            assertTrue(matcher.matches(), "Expected valid phone number: $number")
        }

        invalidNumbers.forEach { number ->
            val matcher = RegExUtils.phoneNumber.matcher(number)
            assertFalse(matcher.matches(), "Expected invalid phone number: $number")
        }
    }

    @Test
    fun testHexColor() {
        val validColors = listOf("#FFF", "#FFFFFF", "#123456", "#1234", "#AABBCCDD")
        val invalidColors = listOf("FFF", "#FFFFFG", "#12345", "#AABBCCDDE")

        validColors.forEach { color ->
            val matcher = RegExUtils.hexColor.matcher(color)
            assertTrue(matcher.matches(), "Expected valid hex color: $color")
        }

        invalidColors.forEach { color ->
            val matcher = RegExUtils.hexColor.matcher(color)
            assertFalse(matcher.matches(), "Expected invalid hex color: $color")
        }
    }
}
