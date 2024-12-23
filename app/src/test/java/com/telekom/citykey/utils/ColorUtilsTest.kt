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
