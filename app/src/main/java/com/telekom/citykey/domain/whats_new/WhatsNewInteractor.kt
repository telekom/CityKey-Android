package com.telekom.citykey.domain.whats_new

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.utils.PreferencesHelper

class WhatsNewInteractor(private val preferencesHelper: PreferencesHelper) {
    companion object {
        //update version name for production build
        const val WIDGET_RELEASE_VERSION_NAME = "1.3.5"
    }

    fun shouldShowWhatsNew() =
        preferencesHelper.isWhatsNewsScreenShown.not() && preferencesHelper.isFirstTime.not() && hasWhatsNewContent()

    private fun hasWhatsNewContent() =
        BuildConfig.APP_VERSION == WIDGET_RELEASE_VERSION_NAME

    fun whatsNewShown() {
        preferencesHelper.isWhatsNewsScreenShown = true
    }
}
