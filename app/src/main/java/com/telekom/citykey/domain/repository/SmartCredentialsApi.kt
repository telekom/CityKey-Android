package com.telekom.citykey.domain.repository

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.requests.RefreshTokenRequest
import com.telekom.citykey.models.user.Credentials
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SmartCredentialsApi {


    fun getNewToken(
        @Body refreshTokenBody: RefreshTokenRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "POST_RefreshToken",
        @Header("Device-Id") androidID: String = "Paycheck",
        @Header("Keep-Me-LoggedIn") keepLoggedIn: Boolean = OAuth2TokenManager.keepMeLoggedIn
    ): Single<OscaResponse<Credentials>>
}
