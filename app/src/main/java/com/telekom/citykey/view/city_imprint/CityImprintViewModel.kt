package com.telekom.citykey.view.city_imprint

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.City
import io.reactivex.BackpressureStrategy

class CityImprintViewModel(private val globalData: GlobalData) : ViewModel() {

    val city: LiveData<City> get() = globalData.city
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()
    val user: LiveData<Boolean> get() = globalData.user
        .map { it is UserState.Present }
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()
}
