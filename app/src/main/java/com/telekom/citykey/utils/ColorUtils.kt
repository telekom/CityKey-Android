package com.telekom.citykey.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlin.math.min
import kotlin.math.roundToInt

object ColorUtils {
    fun setAlpha(@ColorInt color: Int, alpha: Int) =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    fun darken(@ColorInt color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).roundToInt()
        val g = (Color.green(color) * factor).roundToInt()
        val b = (Color.blue(color) * factor).roundToInt()
        return Color.argb(a, min(r, 255), min(g, 255), min(b, 255))
    }

    fun isDark(@ColorInt color: Int): Boolean =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 >= 0.8

    fun invertColor(@ColorInt color: Int): Int = Color.rgb(
        255 - Color.red(color),
        255 - Color.green(color),
        255 - Color.blue(color)
    )

    fun invertIfDark(@ColorInt color: Int): Int =
        if (isDark(color)) invertColor(color) else color
}
