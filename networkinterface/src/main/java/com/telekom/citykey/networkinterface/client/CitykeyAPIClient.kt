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

package com.telekom.citykey.networkinterface.client

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

interface CitykeyAPIClient {

    fun getCityById(cityId: Int): Maybe<OscaResponse<List<City?>>>

    fun getCityWeather(cityId: Int): Maybe<OscaResponse<List<CityWeather>>>

    fun getCityContent(cityId: Int): Maybe<OscaResponse<List<CityContent>>>

    fun login(request: LogInRequest, cityId: Int, keepLoggedIn: Boolean): Maybe<OscaResponse<Credentials>>

    fun register(userRegistration: RegistrationRequest, cityId: Int): Maybe<OscaResponse<RegistrationResponse>>

    fun resendPINEmail(request: ResendPinRequest, cityId: Int, actionName: String): Completable

    fun getAllCities(cityId: Int): Maybe<OscaResponse<MutableList<AvailableCity>>>

    fun getLegalData(cityId: Int): Maybe<OscaResponse<List<Terms>>>

    fun logout(request: LogOutRequest, cityId: Int, keepLoggedIn: Boolean): Completable

    fun requestNewPassword(request: NewPasswordRequest, cityId: Int): Completable

    fun getCityEvents(
        cityId: Int,
        start: String? = null,
        end: String? = null,
        pageNo: Int? = null,
        pageSize: Int? = null,
        categories: ArrayList<Int>? = null,
        eventId: String? = "0"
    ): Single<OscaResponse<List<Event>>>

    fun getCityEventsCount(
        cityId: Int,
        start: String? = null,
        end: String? = null,
        categories: ArrayList<Int>? = null
    ): Maybe<OscaResponse<Int>>

    fun getAllEventCategories(cityId: Int): Maybe<OscaResponse<List<EventCategory>>>

    fun setRegistrationConfirmation(request: PinConfirmationRequest, cityId: Int, actionName: String): Completable

    fun getCityServices(cityId: Int): Maybe<OscaResponse<List<ServicesResponse>>>

    fun getNearestCity(longitude: String, latitude: String, cityId: Int): Maybe<OscaResponse<List<NearestCity>>>

    fun getWasteCalendarFilterOptions(cityId: Int): Maybe<OscaResponse<List<GetWasteTypeResponse>>>

    fun getWasteAddressDetails(streetName: String, cityId: Int): Observable<OscaResponse<List<FtuWaste>>>

    fun getPoiCategories(cityId: Int): Maybe<OscaResponse<List<PoiCategoryGroup>>>

    fun getPOIs(
        cityId: Int,
        latitude: Double,
        longitude: Double,
        categoryId: Int
    ): Maybe<OscaResponse<List<PointOfInterest>>>

    fun getEgovItems(cityId: Int): Maybe<OscaResponse<List<EgovData>>>

    fun getEgovItemsGroup(cityId: Int): Maybe<OscaResponse<List<EgovGroup>>>

    fun getDefectCategories(cityId: Int): Maybe<OscaResponse<List<DefectCategory>>>

    fun sendDefectRequest(defectRequest: DefectRequest, cityId: Int): Maybe<OscaResponse<DefectSuccess>>

    fun getServiceDetailInfo(cityId: Int, cityServiceId: Int): Maybe<OscaResponse<List<DetailHelpInfo>>>

    fun sendImage(cityId: Int, image: RequestBody): Maybe<OscaResponse<DefectImage>>

    fun sendFeedback(feedbackRequest: FeedbackRequest, cityId: Int): Completable

    fun checkAppVersion(version: String, cityId: Int): Maybe<OscaResponse<List<Content>>>

    fun getDataPrivacyNoticeForSurvey(cityId: Int): Maybe<OscaResponse<List<DataPrivacyNoticeResponse>>>

    fun getFahrradparkenCategories(cityId: Int): Maybe<OscaResponse<List<DefectCategory>>>

    fun createFahrradparkenReport(
        fahrradparkenRequest: FahrradparkenRequest,
        cityId: Int
    ): Maybe<OscaResponse<DefectSuccess>>

    fun getFahrradparkenExistingReports(
        cityId: Int,
        serviceCode: String,
        boundingBox: String,
        reportCountLimit: Int
    ): Maybe<OscaResponse<List<FahrradparkenReport>>>
}
