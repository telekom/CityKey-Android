package com.telekom.citykey.view.services.appointments

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.view.BaseViewModel
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.BackpressureStrategy

class AppointmentServiceViewModel(
    private val cityInteractor: CityInteractor,
    private val inAppNotificationsInteractor: InAppNotificationsInteractor,
) : BaseViewModel() {

    val cityColor: LiveData<Int>
        get() = cityInteractor.city
            .map { Color.parseColor(it.cityColor) }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    val updates: LiveData<Int>
        get() = inAppNotificationsInteractor.badgesCounter
            .map { it[R.id.services_graph]?.get(ServicesFunctions.TERMINE) ?: 0 }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
}
