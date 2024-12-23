package com.telekom.citykey.domain.mock

import com.telekom.citykey.domain.repository.SmartCityTpnsApi
import com.telekom.citykey.models.api.requests.TpnsRegisterRequest
import io.reactivex.Completable

class SmartCityTpnsApiMockImpl : SmartCityTpnsApi {

    override fun registerForTpns(tpnsRequest: TpnsRegisterRequest): Completable = Completable.complete()
}
