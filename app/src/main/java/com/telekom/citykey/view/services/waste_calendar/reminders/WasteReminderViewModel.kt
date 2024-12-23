package com.telekom.citykey.view.services.waste_calendar.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.notifications.NotificationsStateController
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.waste_calendar.WasteCalendarReminder
import com.telekom.citykey.view.BaseViewModel

class WasteReminderViewModel(
    globalData: GlobalData,
    private val wasteCalendarInteractor: WasteCalendarInteractor,
    private val notificationsStateController: NotificationsStateController,
    private val adjustManager: AdjustManager
) : BaseViewModel() {

    private val _reminder: MutableLiveData<WasteCalendarReminder> = MutableLiveData()
    private val _cityColor: MutableLiveData<Int> = MutableLiveData(globalData.cityColor)

    val reminder: LiveData<WasteCalendarReminder> get() = _reminder
    val cityColor: LiveData<Int> get() = _cityColor
    val areOsNotificationsEnabled: LiveData<Boolean> get() = cityColor.switchMap { notificationsStateController.areOsNotificationsEnabled }

    fun onViewCreated(wasteTypeId: Int) {
        launch {
            wasteCalendarInteractor.findReminder(wasteTypeId)
                .subscribe(_reminder::postValue)
        }
    }

    fun onViewResumedOrSettingsApplied() {
        notificationsStateController.requestNotificationSettings()
    }

    fun onReminderDone(wasteTypeId: Int, time: String, sameDay: Boolean, dayBefore: Boolean, twoDaysBefore: Boolean) {
        val reminder = WasteCalendarReminder(wasteTypeId, time, sameDay, dayBefore, twoDaysBefore)
        if (reminder != _reminder.value) {
            wasteCalendarInteractor.saveReminder(reminder)
            if (reminder.sameDay || reminder.oneDayBefore || reminder.twoDaysBefore)
                adjustManager.trackEvent(R.string.set_waste_reminder)
        }
    }
}
