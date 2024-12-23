package com.telekom.citykey.domain.repository

import com.telekom.citykey.models.api.requests.TpnsRegisterRequest
import io.reactivex.Completable
import retrofit2.http.Body

interface SmartCityTpnsApi {

    fun registerForTpns(
        @Body tpnsRequest: TpnsRegisterRequest
    ): Completable
}
