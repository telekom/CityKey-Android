package com.telekom.citykey.utils.extensions

import android.os.Build
import com.telekom.citykey.BuildConfig

val isQaBuild get() = BuildConfig.FLAVOR == "qa"

val isProdBuild get() = BuildConfig.FLAVOR == "production"

val shouldPreventContentSharing get() = isQaBuild.not() && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
