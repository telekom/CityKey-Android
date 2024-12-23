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
