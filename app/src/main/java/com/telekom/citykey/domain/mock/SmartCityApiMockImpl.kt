package com.telekom.citykey.domain.mock

import com.telekom.citykey.domain.repository.SmartCityApi
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.requests.DefectRequest
import com.telekom.citykey.models.api.requests.FahrradparkenRequest
import com.telekom.citykey.models.api.requests.FeedbackRequest
import com.telekom.citykey.models.api.requests.LogInRequest
import com.telekom.citykey.models.api.requests.LogOutRequest
import com.telekom.citykey.models.api.requests.NewPasswordRequest
import com.telekom.citykey.models.api.requests.PinConfirmationRequest
import com.telekom.citykey.models.api.requests.RegistrationRequest
import com.telekom.citykey.models.api.requests.ResendPinRequest
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.models.content.City
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.models.content.CityWeather
import com.telekom.citykey.models.content.Content
import com.telekom.citykey.models.content.DataPrivacyNoticeResponse
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.models.content.EventCategory
import com.telekom.citykey.models.content.NearestCity
import com.telekom.citykey.models.content.RegistrationResponse
import com.telekom.citykey.models.content.ServicesResponse
import com.telekom.citykey.models.content.Terms
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

private const val GET_CITY_BY_ID = "get_city_by_id"
private const val GET_CITY_WEATHER = "get_city_weather"
private const val GET_CITY_CONTENT = "get_news_for_city_content"

private const val LOGIN = "login"
private const val REGISTER = "register"

private const val GET_ALL_CITIES = "get_all_cities"
private const val GET_LEGAL_DATA = "get_legal_data"
private const val GET_CITY_EVENTS = "get_city_events"
private const val GET_CITY_SERVICES = "get_city_services"
private const val GET_CITY_SERVICE_DETAIL_INFO = "get_city_service_detail_info"
private const val GET_ALL_EVENT_CATEGORIES = "get_all_event_categories"
private const val GET_NEAREST_CITY = "get_nearest_city"

private const val GET_WASTE_CALENDAR_FILTER_OPTIONS = "get_waste_calendar_filter_options"
private const val GET_WASTE_ADDRESS_DETAILS = "get_waste_address_details"

private const val GET_POI_CATEGORIES = "get_poi_categories"
private const val GET_POIS = "get_pois"

private const val GET_EGOV_ITEMS = "get_egov_items"

private const val GET_DEFECT_CATEGORIES = "get_defect_categories"
private const val SEND_DEFECT_REQUEST = "send_defect_request"
private const val SEND_IMAGE = "send_image"

private const val GET_SURVEY_DPN = "get_survey_dpn"

private const val GET_FAHRRAD_PARKEN_REPORTS = "get_fahrrad_parken_reports"
private const val GET_FAHRRAD_PARKEN_CATEGORIES = "get_fahrrad_parken_categories"
private const val CREATE_FAHRRAD_PARKEN_REPORT = "create_fahrrad_parken_report"

private const val CHECK_APP_VERSION = "check_app_version"

class SmartCityApiMockImpl(
    private val assetResponseMocker: AssetResponseMocker
) : SmartCityApi {

    override fun getCityById(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<City?>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_BY_ID)
    )

    override fun getCityWeather(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<CityWeather>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_WEATHER)
    )

    override fun getCityContent(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<CityContent>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_CONTENT)
    )

    override fun login(
        request: LogInRequest,
        cityId: Int,
        actionName: String,
        androidID: String,
        keepLoggedIn: Boolean
    ): Maybe<OscaResponse<Credentials>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(LOGIN)
    )

    override fun register(
        userRegistration: RegistrationRequest,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<RegistrationResponse>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(REGISTER)
    )

    override fun resendPINEmail(
        request: ResendPinRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getAllCities(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<AvailableCity>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_ALL_CITIES)
    )

    override fun getLegalData(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<Terms>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_LEGAL_DATA)
    )

    override fun logout(
        request: LogOutRequest,
        cityId: Int,
        actionName: String,
        androidID: String,
        keepLoggedIn: Boolean
    ): Completable = Completable.complete()

    override fun requestNewPassword(
        request: NewPasswordRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getCityEvents(
        cityId: Int,
        start: String?,
        end: String?,
        pageNo: Int?,
        pageSize: Int?,
        categories: ArrayList<Int>?,
        eventId: String?,
        actionName: String
    ): Single<OscaResponse<List<Event>>> = Single.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_EVENTS)
    )

    override fun getCityEventsCount(
        cityId: Int,
        start: String?,
        end: String?,
        categories: ArrayList<Int>?,
        actionName: String
    ): Maybe<OscaResponse<Int>> = Maybe.just(OscaResponse(4458))

    override fun getAllEventCategories(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<EventCategory>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_ALL_EVENT_CATEGORIES)
    )

    override fun setRegistrationConfirmation(
        request: PinConfirmationRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getCityServices(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<ServicesResponse>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_SERVICES)
    )

    override fun getNearestCity(
        longitude: String,
        latitude: String,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<NearestCity>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_NEAREST_CITY)
    )

    override fun getWasteCalendarFilterOptions(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<GetWasteTypeResponse>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_WASTE_CALENDAR_FILTER_OPTIONS)
    )

    override fun getWasteAddressDetails(
        streetName: String,
        cityId: Int,
        actionName: String
    ): Observable<OscaResponse<List<FtuWaste>>> = Observable.just(
        assetResponseMocker.getOscaResponseListOf(GET_WASTE_ADDRESS_DETAILS)
    )

    override fun getPoiCategories(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<PoiCategoryGroup>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_POI_CATEGORIES)
    )

    override fun getPOIs(
        cityId: Int,
        latitude: Double,
        longitude: Double,
        categoryId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<PointOfInterest>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_POIS)
    )

    override fun getEgovItems(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<EgovGroup>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_EGOV_ITEMS)
    )

    override fun getDefectCategories(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<DefectCategory>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_DEFECT_CATEGORIES)
    )

    override fun sendDefectRequest(
        defectRequest: DefectRequest,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<DefectSuccess>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(SEND_DEFECT_REQUEST)
    )

    override fun getServiceDetailInfo(
        cityId: Int,
        cityServiceId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<DetailHelpInfo>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_SERVICE_DETAIL_INFO)
    )

    override fun sendImage(
        cityId: Int,
        image: RequestBody,
        actionName: String
    ): Maybe<OscaResponse<DefectImage>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(SEND_IMAGE)
    )

    override fun sendFeedback(
        feedbackRequest: FeedbackRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun checkAppVersion(
        version: String,
        os: String,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<Content>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(CHECK_APP_VERSION)
    )

    override fun getDataPrivacyNoticeForSurvey(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<DataPrivacyNoticeResponse>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_SURVEY_DPN)
    )

    override fun getFahrradparkenCategories(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<DefectCategory>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_FAHRRAD_PARKEN_CATEGORIES)
    )

    override fun createFahrradparkenReport(
        fahrradparkenRequest: FahrradparkenRequest,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<DefectSuccess>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(CREATE_FAHRRAD_PARKEN_REPORT)
    )

    override fun getFahrradparkenExistingReports(
        cityId: Int,
        serviceCode: String,
        boundingBox: String,
        reportCountLimit: Int,
        actionName: String
    ): Maybe<OscaResponse<List<FahrradparkenReport>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_FAHRRAD_PARKEN_REPORTS)
    )
}
