/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.domain.repository

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.requests.*
import com.telekom.citykey.models.content.*
import com.telekom.citykey.models.defect_reporter.DefectCategory
import com.telekom.citykey.models.defect_reporter.DefectImage
import com.telekom.citykey.models.defect_reporter.DefectSuccess
import com.telekom.citykey.models.egov.DetailHelpInfo
import com.telekom.citykey.models.egov.EgovGroup
import com.telekom.citykey.models.fahrradparken.FahrradparkenReport
import com.telekom.citykey.models.poi.PoiCategoryGroup
import com.telekom.citykey.models.poi.PointOfInterest
import com.telekom.citykey.models.user.Credentials
import com.telekom.citykey.models.waste_calendar.FtuWaste
import com.telekom.citykey.models.waste_calendar.GetWasteTypeResponse
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface SmartCityApi {


    @ErrorType(OscaErrorResponse::class)
    fun getCityById(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_CityData"
    ): Maybe<OscaResponse<List<City?>>>


    fun getCityWeather(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_Weather"
    ): Maybe<OscaResponse<List<CityWeather>>>


    @ErrorType(OscaErrorResponse::class)
    fun getCityContent(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_News"
    ): Maybe<OscaResponse<List<CityContent>>>


    @ErrorType(OscaErrorResponse::class)
    fun login(
        @Body request: LogInRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "POST_Login",
        @Header("Device-Id") androidID: String = "Paycheck",
        @Header("Keep-Me-LoggedIn") keepLoggedIn: Boolean
    ): Maybe<OscaResponse<Credentials>>


    @ErrorType(OscaErrorResponse::class)
    fun register(
        @Body userRegistration: RegistrationRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "POST_RegisterNewUser"
    ): Maybe<OscaResponse<RegistrationResponse>>


    @ErrorType(OscaErrorResponse::class)
    fun resendPINEmail(
        @Body request: ResendPinRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "PUT_ResendVerificationPIN"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun getAllCities(
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "GET_AllCities"
    ): Maybe<OscaResponse<List<AvailableCity>>>


    fun getLegalData(
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "GET_Terms"
    ): Maybe<OscaResponse<List<Terms>>>


    fun logout(
        @Body request: LogOutRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "DELETE_Logout",
        @Header("Device-Id") androidID: String = "Paycheck",
        @Header("Keep-Me-LoggedIn") keepLoggedIn: Boolean = OAuth2TokenManager.keepMeLoggedIn
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun requestNewPassword(
        @Body request: NewPasswordRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "POST_PasswordReset"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun getCityEvents(
        @Query("cityId") cityId: Int,
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("pageNo") pageNo: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("categories") categories: ArrayList<Int>? = null,
        @Query("eventId") eventId: String? = "0",
        @Query("actionName") actionName: String = "GET_Event"
    ): Single<OscaResponse<List<Event>>>


    @ErrorType(OscaErrorResponse::class)
    fun getCityEventsCount(
        @Query("cityId") cityId: Int,
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("categories") categories: ArrayList<Int>? = null,
        @Query("actionName") actionName: String = "GET_EventCount"
    ): Maybe<OscaResponse<Int>>


    @ErrorType(OscaErrorResponse::class)
    fun getAllEventCategories(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_EventCategory"
    ): Maybe<OscaResponse<List<EventCategory>>>


    @ErrorType(OscaErrorResponse::class)
    fun setRegistrationConfirmation(
        @Body request: PinConfirmationRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "POST_ValidatePIN"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun getCityServices(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_CityServiceData"
    ): Maybe<OscaResponse<List<ServicesResponse>>>


    @ErrorType(OscaErrorResponse::class)
    fun getNearestCity(
        @Query("LNG") longitude: String,
        @Query("LAT") latitude: String,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "GET_NearestCityId"
    ): Maybe<OscaResponse<List<NearestCity>>>


    @ErrorType(OscaErrorResponse::class)
    fun getWasteCalendarFilterOptions(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_WasteTypeObj"
    ): Maybe<OscaResponse<List<GetWasteTypeResponse>>>


    @ErrorType(OscaErrorResponse::class)
    fun getWasteAddressDetails(
        @Query("filterStr") streetName: String,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_StreetNameAndHouseNumber"
    ): Observable<OscaResponse<List<FtuWaste>>>


    @ErrorType(OscaErrorResponse::class)
    fun getPoiCategories(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_CityPOICategories"
    ): Maybe<OscaResponse<List<PoiCategoryGroup>>>


    @ErrorType(OscaErrorResponse::class)
    fun getPOIs(
        @Query("cityId") cityId: Int,
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("categoryId") categoryId: Int,
        @Query("actionName") actionName: String = "GET_CityPOI",
    ): Maybe<OscaResponse<List<PointOfInterest>>>


    @ErrorType(OscaErrorResponse::class)
    fun getEgovItems(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_CityServices"
    ): Maybe<OscaResponse<List<EgovGroup>>>


    @ErrorType(OscaErrorResponse::class)
    fun getDefectCategories(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_CityDefectCategories"
    ): Maybe<OscaResponse<List<DefectCategory>>>


    @ErrorType(OscaErrorResponse::class)
    fun sendDefectRequest(
        @Body defectRequest: DefectRequest,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "POST_CityDefect"
    ): Maybe<OscaResponse<DefectSuccess>>


    @ErrorType(OscaErrorResponse::class)
    fun getServiceDetailInfo(
        @Query("cityId") cityId: Int,
        @Query("cityServiceId") cityServiceId: Int,
        @Query("actionName") actionName: String = "GET_ServiceHelpContent"
    ): Maybe<OscaResponse<List<DetailHelpInfo>>>


    @ErrorType(OscaErrorResponse::class)
    fun sendImage(
        @Query("cityId") cityId: Int,
        @Body image: RequestBody,
        @Query("actionName") actionName: String = "POST_Image",
    ): Maybe<OscaResponse<DefectImage>>


    @ErrorType(OscaErrorResponse::class)
    fun sendFeedback(
        @Body feedbackRequest: FeedbackRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "POST_SubmitFeedback"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun checkAppVersion(
        @Query("appVersion") version: String = BuildConfig.APP_VERSION,
        @Query("osName") os: String = "android",
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "GET_AppVersion"
    ): Maybe<OscaResponse<List<Content>>>


    @ErrorType(OscaErrorResponse::class)
    fun getDataPrivacyNoticeForSurvey(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_DpnText"
    ): Maybe<OscaResponse<List<DataPrivacyNoticeResponse>>>


    @ErrorType(OscaErrorResponse::class)
    fun getFahrradparkenCategories(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_CityFahrradparkenCategories"
    ): Maybe<OscaResponse<List<DefectCategory>>>


    @ErrorType(OscaErrorResponse::class)
    fun createFahrradparkenReport(
        @Body fahrradparkenRequest: FahrradparkenRequest,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "POST_CityFahrradparkenDefect"
    ): Maybe<OscaResponse<DefectSuccess>>


    @ErrorType(OscaErrorResponse::class)
    fun getFahrradparkenExistingReports(
        @Query("cityId") cityId: Int,
        @Query("service_code") serviceCode: String,
        @Query("bbox") boundingBox: String,
        @Query("limit") reportCountLimit: Int,
        @Query("actionName") actionName: String = "GET_FahrradparkenExistingDefects"
    ): Maybe<OscaResponse<List<FahrradparkenReport>>>

}
