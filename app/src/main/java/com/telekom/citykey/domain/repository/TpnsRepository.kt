package com.telekom.citykey.domain.repository

import com.telekom.citykey.models.api.requests.TpnsRegisterRequest
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class TpnsRepository(private val api: SmartCityTpnsApi) {

    fun registerForTpns(tpnsRequest: TpnsRegisterRequest): Completable =
        api.registerForTpns(tpnsRequest)
            .subscribeOn(Schedulers.io())
}
