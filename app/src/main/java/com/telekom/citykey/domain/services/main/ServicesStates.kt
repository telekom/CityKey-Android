package com.telekom.citykey.domain.services.main

import com.telekom.citykey.models.content.ServicesData

sealed class ServicesStates {
    object Loading : ServicesStates()
    object Error : ServicesStates()
    class Success(val data: ServicesData) : ServicesStates()
}
