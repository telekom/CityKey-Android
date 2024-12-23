package com.telekom.citykey.domain.mock

import com.telekom.citykey.domain.repository.SmartCredentialsApi
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.requests.RefreshTokenRequest
import com.telekom.citykey.models.user.Credentials
import io.reactivex.Single

private const val GET_NEW_TOKEN = "login"

class SmartCredentialsApiMockImpl(
    private val assetResponseMocker: AssetResponseMocker
) : SmartCredentialsApi {

    override fun getNewToken(
        refreshTokenBody: RefreshTokenRequest,
        cityId: Int,
        actionName: String,
        androidID: String,
        keepLoggedIn: Boolean
    ): Single<OscaResponse<Credentials>> = Single.just(
        assetResponseMocker.getOscaResponseOf(GET_NEW_TOKEN)
    )
}
