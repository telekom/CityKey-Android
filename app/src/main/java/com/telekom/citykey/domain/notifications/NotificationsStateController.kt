package com.telekom.citykey.domain.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData

class NotificationsStateController(
    private val context: Context
) {
    private val _isOSNotificationsEnabled get() = NotificationManagerCompat.from(context).areNotificationsEnabled()

    val areOsNotificationsEnabled: MutableLiveData<Boolean> = MutableLiveData()

    init {
        requestNotificationSettings()
    }

    fun requestNotificationSettings() {
        areOsNotificationsEnabled.postValue(_isOSNotificationsEnabled)
    }
}
