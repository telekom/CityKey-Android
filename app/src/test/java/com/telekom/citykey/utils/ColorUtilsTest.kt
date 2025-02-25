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
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.utils


import android.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.min

class ColorUtilsTest {

    @Test
    fun `test setAlpha`() {
        val color = Color.rgb(255, 0, 0) // Red color
        val alpha = 128
        val expectedColor = Color.argb(alpha, 255, 0, 0)
        val result = ColorUtils.setAlpha(color, alpha)
        assertEquals(expectedColor, result)
    }

    @Test
    fun `test darken`() {
        val color = Color.rgb(255, 255, 255) // White color
        val factor = 0.5f
        val expectedColor = Color.argb(255, 128, 128, 128) // Darkened white (gray)
        val result = ColorUtils.darken(color, factor)
        assertEquals(expectedColor, result)
    }

    @Test
    fun `test isDark for dark color`() {
        val darkColor = Color.rgb(0, 0, 0) // Black color
        val result = ColorUtils.isDark(darkColor)
        assertEquals(true, result)
    }

    @Test
    fun `test invertColor`() {
        val color = Color.rgb(255, 0, 0) // Red color
        val expectedColor = Color.rgb(0, 255, 255) // Cyan color
        val result = ColorUtils.invertColor(color)
        assertEquals(expectedColor, result)
    }

    @Test
    fun `test invertIfDark for dark color`() {
        val darkColor = Color.rgb(0, 0, 0) // Black color
        val expectedColor = Color.rgb(255, 255, 255) // White color
        val result = ColorUtils.invertIfDark(darkColor)
        assertEquals(expectedColor, result)
    }

    @Test
    fun `test invertIfDark for light color`() {
        val lightColor = Color.rgb(255, 255, 255) // White color
        val result = ColorUtils.invertIfDark(lightColor)
        assertEquals(lightColor, result)
    }


    @Test
    fun `test setAlpha with maximum alpha`() {
        val color = Color.rgb(255, 0, 0) // Red color
        val alpha = 255
        val expectedColor = Color.argb(alpha, 255, 0, 0)
        val result = ColorUtils.setAlpha(color, alpha)
        assertEquals(expectedColor, result)
    }

    @Test
    fun `test setAlpha with minimum alpha`() {
        val color = Color.rgb(255, 0, 0) // Red color
        val alpha = 0
        val expectedColor = Color.argb(alpha, 255, 0, 0)
        val result = ColorUtils.setAlpha(color, alpha)
        assertEquals(expectedColor, result)
    }

    @Test
    fun `test darken with factor greater than 1`() {
        val color = Color.rgb(100, 100, 100) // Grey color
        val factor = 1.5f
        val expectedColor = Color.argb(
            255,
            min((100 * factor).toInt(), 255),
            min((100 * factor).toInt(), 255),
            min((100 * factor).toInt(), 255)
        )
        val result = ColorUtils.darken(color, factor)
        assertEquals(expectedColor, result)
    }

    @Test
    fun `test darken with factor equal to 1`() {
        val color = Color.rgb(100, 100, 100) // Grey color
        val factor = 1.0f
        val result = ColorUtils.darken(color, factor)
        assertEquals(color, result)
    }

    @Test
    fun `test isDark with borderline dark color`() {
        val borderlineDarkColor = Color.rgb(77, 77, 77) // A dark grey color close to the threshold
        val result = ColorUtils.isDark(borderlineDarkColor)
        assertEquals(true, result)
    }

    @Test
    fun `test invertIfDark with slightly dark color`() {
        val slightlyDarkColor = Color.rgb(100, 100, 100) // Slightly dark grey
        val expectedColor = if (ColorUtils.isDark(slightlyDarkColor)) {
            ColorUtils.invertColor(slightlyDarkColor)
        } else {
            slightlyDarkColor
        }
        val result = ColorUtils.invertIfDark(slightlyDarkColor)
        assertEquals(expectedColor, result)
    }
}
