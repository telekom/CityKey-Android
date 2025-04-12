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

import com.telekom.citykey.domain.services.waste_calendar.WasteAddressState
import com.telekom.citykey.networkinterface.client.CitykeyAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyAuthAPIClient
import com.telekom.citykey.networkinterface.models.api.requests.DefectRequest
import com.telekom.citykey.networkinterface.models.api.requests.FahrradparkenRequest
import com.telekom.citykey.networkinterface.models.api.requests.SubmitSurveyRequest
import com.telekom.citykey.networkinterface.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.networkinterface.models.appointments.Appointment
import com.telekom.citykey.networkinterface.models.citizen_survey.Question
import com.telekom.citykey.networkinterface.models.citizen_survey.SubmitResponse
import com.telekom.citykey.networkinterface.models.citizen_survey.Survey
import com.telekom.citykey.networkinterface.models.content.DataPrivacyNoticeResponse
import com.telekom.citykey.networkinterface.models.defect_reporter.DefectCategory
import com.telekom.citykey.networkinterface.models.defect_reporter.DefectSuccess
import com.telekom.citykey.networkinterface.models.egov.DetailHelpInfo
import com.telekom.citykey.networkinterface.models.egov.EgovData
import com.telekom.citykey.networkinterface.models.egov.EgovGroup
import com.telekom.citykey.networkinterface.models.fahrradparken.FahrradparkenReport
import com.telekom.citykey.networkinterface.models.poi.PoiCategoryGroup
import com.telekom.citykey.networkinterface.models.poi.PointOfInterest
import com.telekom.citykey.networkinterface.models.waste_calendar.GetSelectedWastePickupsResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.SaveSelectedWastePickupRequest
import com.telekom.citykey.networkinterface.models.waste_calendar.SaveSelectedWastePickupsResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarReminder
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarResponse
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody

class ServicesRepository(
    private val api: CitykeyAPIClient,
    private val authApi: CitykeyAuthAPIClient,
) {

    /* Surveys */
    fun getSurveyQuestions(surveyId: Int, cityId: Int): Maybe<List<Question>> =
        authApi.getSurveyQuestions(surveyId, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content.questions }
            .map { it.sortedBy { question -> question.questionOrder } }

    fun submitSurvey(cityId: Int, surveyId: Int, request: SubmitSurveyRequest): Maybe<SubmitResponse> =
        authApi.submitSurvey(cityId, surveyId, request)
            .subscribeOn(Schedulers.io())
            .map { it.content }
            .observeOn(AndroidSchedulers.mainThread())

    fun getSurveys(cityId: Int): Maybe<List<Survey>> = authApi.getSurveys(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    /* Defect Reporter */
    fun reportDefect(request: DefectRequest, cityId: Int): Maybe<DefectSuccess> = api.sendDefectRequest(request, cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }
        .observeOn(AndroidSchedulers.mainThread())

    fun uploadImage(cityId: Int, body: RequestBody): Maybe<String> = api.sendImage(cityId, body)
        .subscribeOn(Schedulers.io())
        .map { it.content.mediaURL }

    fun getDefectCategories(cityId: Int): Maybe<List<DefectCategory>> = api.getDefectCategories(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    /* Appointments */
    fun getAppointments(userId: String, cityId: Int): Maybe<List<Appointment>> =
        authApi.getAppointments(userId, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content.sortedBy { appt -> appt.startTime } }

    fun deleteAppointments(isDelete: Boolean, apptId: String, cityId: Int): Completable =
        authApi.deleteAppointments(isDelete, apptId, cityId)
            .subscribeOn(Schedulers.io())

    fun cancelAppointments(apptId: String, cityId: Int): Completable =
        authApi.cancelAppointments(apptId, cityId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun readAppointment(apptIds: String, cityId: Int): Completable = authApi.markAppointmentsAsRead(apptIds, cityId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    /* Waste Calendar */
    fun getWasteCalendar(request: WasteCalendarRequest, cityId: Int): Maybe<WasteCalendarResponse> =
        authApi.getWasteCalendar(request, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getWasteCalendarFilterOptions(cityId: Int): Maybe<List<GetWasteTypeResponse>> =
        api.getWasteCalendarFilterOptions(cityId)
            .subscribeOn(Schedulers.io())
            .map { it -> it.content.sortedBy { it.name } }

    fun saveSelectedWastePickups(
        saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest,
        cityId: Int
    ): Maybe<SaveSelectedWastePickupsResponse> =
        authApi.saveSelectedWastePickups(saveSelectedWastePickupRequest, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getSelectedWastePickups(cityId: Int): Maybe<GetSelectedWastePickupsResponse> =
        authApi.getSelectedWastePickups(cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getWasteAddressDetails(streetName: String, cityId: Int): Observable<WasteAddressState> =
        api.getWasteAddressDetails(streetName, cityId)
            .subscribeOn(Schedulers.io())
            .map<WasteAddressState> { WasteAddressState.Success(it.content) }
            .onErrorResumeNext { throwable: Throwable -> Observable.just(WasteAddressState.Error(throwable)) }

    fun saveWasteCalendarReminder(request: WasteCalendarReminder, cityId: Int): Completable =
        authApi.saveWasteCalendarReminder(request, cityId)
            .subscribeOn(Schedulers.io())

    fun getPOIs(cityId: Int, latitude: Double, longitude: Double, categoryId: Int): Maybe<List<PointOfInterest>> =
        api.getPOIs(cityId, latitude, longitude, categoryId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getEgovItems(cityId: Int): Maybe<List<EgovData>> = api.getEgovItems(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun getEgovItemsGroup(cityId: Int): Maybe<List<EgovGroup>> = api.getEgovItemsGroup(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun getPoiCategories(cityId: Int): Maybe<List<PoiCategoryGroup>> = api.getPoiCategories(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun getServiceDetailInfo(cityId: Int, cityServiceId: Int): Maybe<DetailHelpInfo> =
        api.getServiceDetailInfo(cityId, cityServiceId)
            .subscribeOn(Schedulers.io())
            .map { it.content[0] }

    fun getDataPrivacyNoticeForSurvey(cityId: Int): Maybe<DataPrivacyNoticeResponse> =
        api.getDataPrivacyNoticeForSurvey(cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content[0] }

    /* Fahrradparken */
    fun getFahrradparkenCategories(cityId: Int): Maybe<List<DefectCategory>> = api.getFahrradparkenCategories(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun createFahrradparkenReport(request: FahrradparkenRequest, cityId: Int): Maybe<DefectSuccess> =
        api.createFahrradparkenReport(request, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content }
            .observeOn(AndroidSchedulers.mainThread())

    fun getFahrradparkenExistingReports(
        cityId: Int,
        serviceCode: String,
        boundingBox: String,
        reportCountLimit: Int
    ): Maybe<List<FahrradparkenReport>> =
        api.getFahrradparkenExistingReports(cityId, serviceCode, boundingBox, reportCountLimit)
            .subscribeOn(Schedulers.io())
            .map { it.content }
}
