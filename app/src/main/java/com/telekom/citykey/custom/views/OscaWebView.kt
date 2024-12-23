package com.telekom.citykey.custom.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.telekom.citykey.utils.isDarkMode

@SuppressLint("SetJavaScriptEnabled")
class OscaWebView : WebView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defRes: Int)
            : super(context, attrs, defStyle, defRes)

    init {
        if (isInEditMode.not()) initialise()
    }

    private fun initialise() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, resources.isDarkMode)
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            @Suppress("DEPRECATION")
            WebSettingsCompat.setForceDark(
                settings,
                if (resources.isDarkMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
            )
        }
        settings.javaScriptEnabled = true
    }
}
