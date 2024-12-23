package com.telekom.citykey.view.city_selection

import androidx.annotation.StringRes
import com.telekom.citykey.models.content.AvailableCity

sealed class Cities {
    class City(val city: AvailableCity) : Cities()
    class NearestCity(val city: AvailableCity) : Cities()
    class Header(@StringRes val title: Int) : Cities()
    object NoPermission : Cities()
    object Error : Cities()
    object Progress : Cities()
    object ContactLink : Cities()
}
