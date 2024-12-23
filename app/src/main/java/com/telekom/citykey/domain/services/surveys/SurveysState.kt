package com.telekom.citykey.domain.services.surveys

sealed class SurveysState {
    object Success : SurveysState()
    object Loading : SurveysState()
    object Error : SurveysState()
    object Empty : SurveysState()
    object ServiceNotAvailable : SurveysState()
}
