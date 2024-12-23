package com.telekom.citykey.utils.extensions

import android.content.res.Configuration
import androidx.fragment.app.Fragment

/**
 * Returns if this [Fragment]'s orientation is set to Landscape or not
 * `true` for Landscape, `false` otherwise!
 */
val Fragment.isInLandscapeOrientation: Boolean
    get() {
        val orientation = resources.configuration.orientation
        return Configuration.ORIENTATION_LANDSCAPE == orientation
    }
