package com.telekom.citykey.domain.services.appointments

sealed class AppointmentsState {
    object Success : AppointmentsState()
    object Loading : AppointmentsState()
    object Error : AppointmentsState()
    object Empty : AppointmentsState()
    object UserNotLoggedIn : AppointmentsState()
}
