package com.telekom.citykey.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.models.poi.PoiCategory
import com.telekom.citykey.view.user.login.LogoutReason

class PreferencesHelper(val sharedPreferences: SharedPreferences) {

    companion object {
        private const val PREF_PREVIOUS_APP_VERSION = "PREF_PREVIOUS_APP_VERSION"
        private const val PREF_PREVIEW_MODE = "PREF_PREVIEW_MODE"
        private const val PREF_FIRST_TIME = "PREF_FIRST_TIME"
        private const val PREF_SELECTED_CITY_ID = "PREF_SELECTED_CITY_ID"
        private const val PREF_SELECTED_CITY_NAME = "PREF_SELECTED_CITY_NAME"
        private const val PREF_USER_POSTAL_CODE = "PREF_USER_POSTAL_CODE"
        private const val PREF_DEEPLINK_CITY_ID = "PREF_DEEPLINK_CITY_ID"
        private const val PREF_LOGOUT_REASON = "PREF_LOGOUT_REASON"
        private const val PREF_LEGAL_DATA = "PREF_LEGAL_DATA"
        private const val PREF_SHOWED_CITY_SELECTION = "SHOWED_CITY_SELECTION"
        private const val PREF_CONFIRMED_TRACKING_TERMS = "PREF_CONFIRMED_TRACKING_TERMS"
        private const val KMLI_VALUE = "KMLI_VALUE"
        private const val PREF_WHATS_NEW_SHOWN = "PREF_WHATS_NEW_SHOWN"
        private const val PREF_TRACKING_ADJUST_ALLOWED = "PREF_TRACKING_ADJUST_ALLOWED"
        private const val PREF_USER_PROFILE = "PREF_USER_PROFILE"
        private const val PREF_APP_BACKGROUNDING_TIMESTAMP = "PREF_APP_BACKGROUNDING_TIMESTAMP"
    }

    val isTrackingConfirmed: Boolean get() = sharedPreferences.getBoolean(PREF_CONFIRMED_TRACKING_TERMS, false)
    val legalData: String get() = sharedPreferences.getString(PREF_LEGAL_DATA, "") ?: ""
    val isPreviewMode: Boolean get() = sharedPreferences.getBoolean(PREF_PREVIEW_MODE, false)
    val isFirstTime: Boolean get() = sharedPreferences.getBoolean(PREF_FIRST_TIME, true)
    val logoutReason: LogoutReason
        get() = LogoutReason.valueOf(
            sharedPreferences.getString(PREF_LOGOUT_REASON, LogoutReason.INVALID.name)!!
        )
    var isWhatsNewsScreenShown: Boolean
        get() = sharedPreferences.getBoolean(PREF_WHATS_NEW_SHOWN, false)
        set(value) {
            sharedPreferences.edit()
                .putBoolean(PREF_WHATS_NEW_SHOWN, value)
                .apply()
        }

    fun getKeepMeLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KMLI_VALUE, true)
    }

    fun saveKeepMeLoggedIn(keepMeLoggedIn: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KMLI_VALUE, keepMeLoggedIn)
            apply()
        }
    }

    fun togglePreviewMode(value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(PREF_PREVIEW_MODE, value)
            .apply()
    }

    fun setFirstTimeFinished() {
        sharedPreferences.edit()
            .putBoolean(PREF_FIRST_TIME, false)
            .apply()
    }

    fun saveLegalData(data: String) {
        sharedPreferences.edit().apply {
            putString(PREF_LEGAL_DATA, data)
            apply()
        }
    }

    fun saveShowedCitySelectionToolTip() {
        sharedPreferences.edit().apply {
            putBoolean(PREF_SHOWED_CITY_SELECTION, true)
            apply()
        }
    }

    fun getShowedCitySelectionToolTip() = sharedPreferences.getBoolean(PREF_SHOWED_CITY_SELECTION, false)

    fun saveUserProfile(userProfile: UserProfile) {
        sharedPreferences.edit().apply {
            putString(PREF_USER_PROFILE, Gson().toJson(userProfile))
            apply()
        }
    }

    fun getUserProfile(): UserProfile? =
        Gson().fromJson(sharedPreferences.getString(PREF_USER_PROFILE, ""), UserProfile::class.java)

    fun clearUserProfile() = sharedPreferences.edit().remove(PREF_USER_PROFILE).apply()

    fun setSelectedCityId(cityId: Int?) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_SELECTED_CITY_ID, cityId ?: -1)
        editor.apply()
    }

    fun getSelectedCityId(): Int {
        return sharedPreferences.getInt(PREF_SELECTED_CITY_ID, -1)
    }

    fun setSelectedCityName(cityName: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_SELECTED_CITY_NAME, cityName)
        editor.apply()
    }

    fun getSelectedCityName() = sharedPreferences.getString(PREF_SELECTED_CITY_NAME, "")

    fun setUserPostalCode(userPostalCode: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_USER_POSTAL_CODE, userPostalCode)
        editor.apply()
    }

    fun getUserPostalCode() = sharedPreferences.getString(PREF_USER_POSTAL_CODE, "")

    fun setAdjustTrackingAllowed(isTrackingAllowed: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_TRACKING_ADJUST_ALLOWED, isTrackingAllowed)
        editor.apply()
    }

    fun getAdjustTrackingAllowed() = sharedPreferences.getBoolean(PREF_TRACKING_ADJUST_ALLOWED, false)

    fun updateAppVersion(appVersion: String) {
        sharedPreferences.edit().putString(PREF_PREVIOUS_APP_VERSION, appVersion).apply()
    }

    fun getSavedAppVersion() = sharedPreferences.getString(PREF_PREVIOUS_APP_VERSION, null)

    fun setDeepLinkCityId(cityId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_DEEPLINK_CITY_ID, cityId)
        editor.apply()
    }

    fun getDeepLinkCityId(): Int {
        return sharedPreferences.getInt(PREF_DEEPLINK_CITY_ID, 0)
    }

    fun savePoiCategory(categoryKey: String, category: PoiCategory) {
        sharedPreferences.edit().apply {
            putString(categoryKey, Gson().toJson(category))
            apply()
        }
    }

    fun getPoiCategory(categoryKey: String): PoiCategory? =
        Gson().fromJson(sharedPreferences.getString(categoryKey, ""), PoiCategory::class.java)

    fun setLogoutReason(reason: LogoutReason) {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_LOGOUT_REASON, reason.name)
        editor.apply()
    }

    fun saveConfirmedTrackingTerms() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_CONFIRMED_TRACKING_TERMS, true)
        editor.apply()
    }

    fun removePref(keys: List<String>) {
        sharedPreferences.edit {
            keys.forEach(this::remove)
        }
    }

    fun saveAppBackgroundingTimestamp(timestamp: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(PREF_APP_BACKGROUNDING_TIMESTAMP, timestamp)
        editor.apply()
    }

    fun getAppBackgroundingTimestamp() = sharedPreferences.getLong(PREF_APP_BACKGROUNDING_TIMESTAMP, -1)

}
