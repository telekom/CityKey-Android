package com.telekom.citykey.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.telekom.citykey.R
import timber.log.Timber

val Resources.isDarkMode get() =
    configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun GoogleMap.tryLoadingNightStyle(context: Context) {
    try {
        val success = setMapStyle(
            MapStyleOptions.loadRawResourceStyle(context, R.raw.night_mode_style)
        )
        if (success.not()) {
            Timber.e("Style parsing for dark mode failed.")
        }
    } catch (e: Resources.NotFoundException) {
        Timber.e("Can't find map style for dark mode. Error: ", e)
    }
}
