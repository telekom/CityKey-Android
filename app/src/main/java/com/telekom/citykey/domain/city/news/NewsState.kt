package com.telekom.citykey.domain.city.news

import com.telekom.citykey.models.content.CityContent

sealed class NewsState {
    object Loading : NewsState()
    object Error : NewsState()
    object ActionError : NewsState()
    class Success(val content: List<CityContent>) : NewsState()
}
