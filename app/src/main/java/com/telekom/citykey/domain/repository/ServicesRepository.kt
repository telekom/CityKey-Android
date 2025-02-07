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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.domain.repository

import com.telekom.citykey.domain.services.waste_calendar.WasteAddressState
import com.telekom.citykey.models.api.requests.DefectRequest
import com.telekom.citykey.models.api.requests.FahrradparkenRequest
import com.telekom.citykey.models.api.requests.SubmitSurveyRequest
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.models.waste_calendar.SaveSelectedWastePickupRequest
import com.telekom.citykey.models.waste_calendar.SaveSelectedWastePickupsResponse
import com.telekom.citykey.models.waste_calendar.WasteCalendarReminder
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody

class ServicesRepository(
    private val api: SmartCityApi,
    private val authApi: SmartCityAuthApi
) {

    /* Surveys */

    fun getSurveyQuestions(surveyId: Int, cityId: Int) = authApi.getSurveyQuestions(surveyId, cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content.questions }
        .map { it.sortedBy { question -> question.questionOrder } }

    fun submitSurvey(cityId: Int, surveyId: Int, request: SubmitSurveyRequest) =
        authApi.submitSurvey(cityId, surveyId, request)
            .subscribeOn(Schedulers.io())
            .map { it.content }
            .observeOn(AndroidSchedulers.mainThread())

    fun getSurveys(cityId: Int) = authApi.getSurveys(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    /* Defect Reporter */
    fun reportDefect(request: DefectRequest, cityId: Int) = api.sendDefectRequest(request, cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }
        .observeOn(AndroidSchedulers.mainThread())

    fun uploadImage(cityId: Int, body: RequestBody) = api.sendImage(cityId, body)
        .subscribeOn(Schedulers.io())
        .map { it.content.mediaURL }

    fun getDefectCategories(cityId: Int) = api.getDefectCategories(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    /* Appointments */
    fun getAppointments(userId: String, cityId: Int) =
        authApi.getAppointments(userId, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content.sortedBy { appt -> appt.startTime } }

    fun deleteAppointments(isDelete: Boolean, apptId: String, cityId: Int) =
        authApi.deleteAppointments(isDelete, apptId, cityId)
            .subscribeOn(Schedulers.io())

    fun cancelAppointments(apptId: String, cityId: Int) =
        authApi.cancelAppointments(apptId, cityId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun readAppointment(apptIds: String, cityId: Int) = authApi.markAppointmentsAsRead(apptIds, cityId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    /* Waste Calendar */
    fun getWasteCalendar(request: WasteCalendarRequest, cityId: Int) =
        authApi.getWasteCalendar(request, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getWasteCalendarFilterOptions(cityId: Int) = api.getWasteCalendarFilterOptions(cityId)
        .subscribeOn(Schedulers.io())
        .map { it -> it.content.sortedBy { it.name } }

    fun saveSelectedWastePickups(
        saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest,
        cityId: Int
    ): Maybe<SaveSelectedWastePickupsResponse> =
        authApi.saveSelectedWastePickups(saveSelectedWastePickupRequest, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getSelectedWastePickups(cityId: Int) = authApi.getSelectedWastePickups(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun getWasteAddressDetails(streetName: String, cityId: Int): Observable<WasteAddressState> =
        api.getWasteAddressDetails(streetName, cityId)
            .subscribeOn(Schedulers.io())
            .map<WasteAddressState> { WasteAddressState.Success(it.content) }
            .onErrorResumeNext { throwable: Throwable -> Observable.just(WasteAddressState.Error(throwable)) }

    fun saveWasteCalendarReminder(request: WasteCalendarReminder, cityId: Int) =
        authApi.saveWasteCalendarReminder(request, cityId)
            .subscribeOn(Schedulers.io())

    fun getPOIs(cityId: Int, latitude: Double, longitude: Double, categoryId: Int) =
        api.getPOIs(cityId, latitude, longitude, categoryId)
            .subscribeOn(Schedulers.io())
            .map { it.content }

    fun getEgovItems(cityId: Int) = api.getEgovItems(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun getPoiCategories(cityId: Int) = api.getPoiCategories(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun getServiceDetailInfo(cityId: Int, cityServiceId: Int) =
        api.getServiceDetailInfo(cityId, cityServiceId)
            .subscribeOn(Schedulers.io())
            .map { it.content[0] }

    fun getDataPrivacyNoticeForSurvey(cityId: Int) = api.getDataPrivacyNoticeForSurvey(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content[0] }

    /* Fahrradparken */
    fun getFahrradparkenCategories(cityId: Int) = api.getFahrradparkenCategories(cityId)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun createFahrradparkenReport(request: FahrradparkenRequest, cityId: Int) =
        api.createFahrradparkenReport(request, cityId)
            .subscribeOn(Schedulers.io())
            .map { it.content }
            .observeOn(AndroidSchedulers.mainThread())

    fun getFahrradparkenExistingReports(cityId: Int, serviceCode: String, boundingBox: String, reportCountLimit: Int) =
        api.getFahrradparkenExistingReports(cityId, serviceCode, boundingBox, reportCountLimit)
            .subscribeOn(Schedulers.io())
            .map { it.content }
}
