package com.telekom.citykey.utils.extensions

import android.graphics.Color
import androidx.annotation.ColorInt
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.RegExUtils

@ColorInt
fun tryParsingColor(colorString: String?, failsafeColor: String = "#1E73D2"): Int {
    if (colorString.isNullOrBlank()) return Color.parseColor(failsafeColor)
    return try {
        Color.parseColor(if (RegExUtils.hexColor.matcher(colorString).matches()) colorString else failsafeColor)
    } catch (e: Exception) {
        Color.parseColor(failsafeColor)
    }
}

@ColorInt
fun tryParsingColorStringToInt(colorString: String?, failsafeColor: Int = CityInteractor.cityColorInt): Int {
    if (colorString.isNullOrBlank()) return failsafeColor
    return try {
        if (RegExUtils.hexColor.matcher(colorString).matches()) Color.parseColor(colorString) else failsafeColor
    } catch (e: Exception) {
        failsafeColor
    }
}
