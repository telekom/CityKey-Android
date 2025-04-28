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

package com.telekom.citykey.network.impl

import com.telekom.citykey.network.mock.AssetResponseMocker
import com.telekom.citykey.networkinterface.client.CitykeyAPIClient
import com.telekom.citykey.networkinterface.models.OscaResponse
import com.telekom.citykey.networkinterface.models.api.requests.DefectRequest
import com.telekom.citykey.networkinterface.models.api.requests.FahrradparkenRequest
import com.telekom.citykey.networkinterface.models.api.requests.FeedbackRequest
import com.telekom.citykey.networkinterface.models.api.requests.LogInRequest
import com.telekom.citykey.networkinterface.models.api.requests.LogOutRequest
import com.telekom.citykey.networkinterface.models.api.requests.NewPasswordRequest
import com.telekom.citykey.networkinterface.models.api.requests.PinConfirmationRequest
import com.telekom.citykey.networkinterface.models.api.requests.RegistrationRequest
import com.telekom.citykey.networkinterface.models.api.requests.ResendPinRequest
import com.telekom.citykey.networkinterface.models.content.AvailableCity
import com.telekom.citykey.networkinterface.models.content.City
import com.telekom.citykey.networkinterface.models.content.CityContent
import com.telekom.citykey.networkinterface.models.content.CityWeather
import com.telekom.citykey.networkinterface.models.content.Content
import com.telekom.citykey.networkinterface.models.content.DataPrivacyNoticeResponse
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.networkinterface.models.content.EventCategory
import com.telekom.citykey.networkinterface.models.content.NearestCity
import com.telekom.citykey.networkinterface.models.content.RegistrationResponse
import com.telekom.citykey.networkinterface.models.content.ServicesResponse
import com.telekom.citykey.networkinterface.models.content.Terms
import com.telekom.citykey.networkinterface.models.defect_reporter.DefectCategory
import com.telekom.citykey.networkinterface.models.defect_reporter.DefectImage
import com.telekom.citykey.networkinterface.models.defect_reporter.DefectSuccess
import com.telekom.citykey.networkinterface.models.egov.DetailHelpInfo
import com.telekom.citykey.networkinterface.models.egov.EgovData
import com.telekom.citykey.networkinterface.models.egov.EgovGroup
import com.telekom.citykey.networkinterface.models.fahrradparken.FahrradparkenReport
import com.telekom.citykey.networkinterface.models.poi.PoiCategoryGroup
import com.telekom.citykey.networkinterface.models.poi.PointOfInterest
import com.telekom.citykey.networkinterface.models.user.Credentials
import com.telekom.citykey.networkinterface.models.waste_calendar.FtuWaste
import com.telekom.citykey.networkinterface.models.waste_calendar.GetWasteTypeResponse
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
private const val GET_EGOV_ITEMS_GROUPS = "get_egov_items_groups"

private const val GET_DEFECT_CATEGORIES = "get_defect_categories"
private const val SEND_DEFECT_REQUEST = "send_defect_request"
private const val SEND_IMAGE = "send_image"

private const val GET_SURVEY_DPN = "get_survey_dpn"

private const val GET_FAHRRAD_PARKEN_REPORTS = "get_fahrrad_parken_reports"
private const val GET_FAHRRAD_PARKEN_CATEGORIES = "get_fahrrad_parken_categories"
private const val CREATE_FAHRRAD_PARKEN_REPORT = "create_fahrrad_parken_report"

private const val CHECK_APP_VERSION = "check_app_version"

internal class CitykeyAPIClientImpl(
    private val assetResponseMocker: AssetResponseMocker
) : CitykeyAPIClient {

    override fun getCityById(cityId: Int): Maybe<OscaResponse<List<City?>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_BY_ID)
    )

    override fun getCityWeather(cityId: Int): Maybe<OscaResponse<List<CityWeather>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_WEATHER)
    )

    override fun getCityContent(cityId: Int): Maybe<OscaResponse<List<CityContent>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_CONTENT)
    )

    override fun login(
        request: LogInRequest,
        cityId: Int,
        keepLoggedIn: Boolean
    ): Maybe<OscaResponse<Credentials>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(LOGIN)
    )

    override fun register(
        userRegistration: RegistrationRequest,
        cityId: Int
    ): Maybe<OscaResponse<RegistrationResponse>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(REGISTER)
    )

    override fun resendPINEmail(
        request: ResendPinRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getAllCities(cityId: Int): Maybe<OscaResponse<MutableList<AvailableCity>>> {
        val result: OscaResponse<List<AvailableCity>> = assetResponseMocker.getOscaResponseListOf(GET_ALL_CITIES)
        val mutableListMappedResponse: OscaResponse<MutableList<AvailableCity>> =
            OscaResponse(result.content.toMutableList())
        return Maybe.just(mutableListMappedResponse)
    }

    override fun getLegalData(cityId: Int): Maybe<OscaResponse<List<Terms>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_LEGAL_DATA)
    )

    override fun logout(
        request: LogOutRequest,
        cityId: Int,
        keepLoggedIn: Boolean
    ): Completable = Completable.complete()

    override fun requestNewPassword(
        request: NewPasswordRequest,
        cityId: Int
    ): Completable = Completable.complete()

    override fun getCityEvents(
        cityId: Int,
        start: String?,
        end: String?,
        pageNo: Int?,
        pageSize: Int?,
        categories: ArrayList<Int>?,
        eventId: String?
    ): Single<OscaResponse<List<Event>>> = Single.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_EVENTS)
    )

    override fun getCityEventsCount(
        cityId: Int,
        start: String?,
        end: String?,
        categories: ArrayList<Int>?
    ): Maybe<OscaResponse<Int>> = Maybe.just(OscaResponse(4458))

    override fun getAllEventCategories(
        cityId: Int
    ): Maybe<OscaResponse<List<EventCategory>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_ALL_EVENT_CATEGORIES)
    )

    override fun setRegistrationConfirmation(
        request: PinConfirmationRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getCityServices(cityId: Int): Maybe<OscaResponse<List<ServicesResponse>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_SERVICES)
    )

    override fun getNearestCity(
        longitude: String,
        latitude: String,
        cityId: Int
    ): Maybe<OscaResponse<List<NearestCity>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_NEAREST_CITY)
    )

    override fun getWasteCalendarFilterOptions(
        cityId: Int
    ): Maybe<OscaResponse<List<GetWasteTypeResponse>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_WASTE_CALENDAR_FILTER_OPTIONS)
    )

    override fun getWasteAddressDetails(
        streetName: String,
        cityId: Int
    ): Observable<OscaResponse<List<FtuWaste>>> = Observable.just(
        assetResponseMocker.getOscaResponseListOf(GET_WASTE_ADDRESS_DETAILS)
    )

    override fun getPoiCategories(
        cityId: Int
    ): Maybe<OscaResponse<List<PoiCategoryGroup>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_POI_CATEGORIES)
    )

    override fun getPOIs(
        cityId: Int,
        latitude: Double,
        longitude: Double,
        categoryId: Int
    ): Maybe<OscaResponse<List<PointOfInterest>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_POIS)
    )

    override fun getEgovItems(cityId: Int): Maybe<OscaResponse<List<EgovData>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_EGOV_ITEMS)
    )

    override fun getEgovItemsGroup(cityId: Int): Maybe<OscaResponse<List<EgovGroup>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_EGOV_ITEMS_GROUPS)
    )

    override fun getDefectCategories(
        cityId: Int
    ): Maybe<OscaResponse<List<DefectCategory>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_DEFECT_CATEGORIES)
    )

    override fun sendDefectRequest(
        defectRequest: DefectRequest,
        cityId: Int
    ): Maybe<OscaResponse<DefectSuccess>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(SEND_DEFECT_REQUEST)
    )

    override fun getServiceDetailInfo(
        cityId: Int,
        cityServiceId: Int
    ): Maybe<OscaResponse<List<DetailHelpInfo>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_CITY_SERVICE_DETAIL_INFO)
    )

    override fun sendImage(
        cityId: Int,
        image: RequestBody
    ): Maybe<OscaResponse<DefectImage>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(SEND_IMAGE)
    )

    override fun sendFeedback(
        feedbackRequest: FeedbackRequest,
        cityId: Int
    ): Completable = Completable.complete()

    override fun checkAppVersion(
        version: String,
        cityId: Int
    ): Maybe<OscaResponse<List<Content>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(CHECK_APP_VERSION)
    )

    override fun getDataPrivacyNoticeForSurvey(
        cityId: Int
    ): Maybe<OscaResponse<List<DataPrivacyNoticeResponse>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_SURVEY_DPN)
    )

    override fun getFahrradparkenCategories(
        cityId: Int
    ): Maybe<OscaResponse<List<DefectCategory>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_FAHRRAD_PARKEN_CATEGORIES)
    )

    override fun createFahrradparkenReport(
        fahrradparkenRequest: FahrradparkenRequest,
        cityId: Int
    ): Maybe<OscaResponse<DefectSuccess>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(CREATE_FAHRRAD_PARKEN_REPORT)
    )

    override fun getFahrradparkenExistingReports(
        cityId: Int,
        serviceCode: String,
        boundingBox: String,
        reportCountLimit: Int
    ): Maybe<OscaResponse<List<FahrradparkenReport>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_FAHRRAD_PARKEN_REPORTS)
    )
}
