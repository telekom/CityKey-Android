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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.domain.repository

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.requests.*
import com.telekom.citykey.models.appointments.Appointment
import com.telekom.citykey.models.citizen_survey.SubmitResponse
import com.telekom.citykey.models.citizen_survey.Survey
import com.telekom.citykey.models.citizen_survey.SurveyQuestions
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.models.user.InfoBoxContent
import com.telekom.citykey.models.user.ResidenceValidationResponse
import com.telekom.citykey.models.waste_calendar.*
import io.reactivex.Completable
import io.reactivex.Maybe
import retrofit2.http.*

interface SmartCityAuthApi {

    fun setInformationRead(
        @Query("msgId") msgId: Int,
        @Query("markRead") markRead: Boolean,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "PUT_InfoBox"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun getUserProfile(
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "GET_UserProfile"
    ): Maybe<OscaResponse<UserProfile>>


    @ErrorType(OscaErrorResponse::class)
    fun changePassword(
        @Body passwordReset: PasswordChangeRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "PUT_ChangePassword"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun changeEmail(
        @Body request: EmailChangeRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "PUT_ChangeEmail"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun changePersonalData(
        @Body request: PersonalDetailChangeRequest,
        @Query("update") update: String,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "PUT_ChangePersonalData"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun validatePostalCode(
        @Query("postalCode") postalCode: String,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "GET_PostalCodeValidation"
    ): Maybe<OscaResponse<ResidenceValidationResponse>>


    @ErrorType(OscaErrorResponse::class)
    fun deleteUser(
        @Body request: DeleteAccountRequest,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "DELETE_UserAccount"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun setEventFavored(
        @Query("markFavorite") markFavorite: Boolean,
        @Query("eventId") eventId: Long,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "POST_EventFavorite"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun getFavoredEvents(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_EventFavorite"
    ): Maybe<OscaResponse<List<Event>>>


    @ErrorType(OscaErrorResponse::class)
    fun getInfoBox(
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "GET_InfoBox"
    ): Maybe<OscaResponse<List<InfoBoxContent>>>


    @ErrorType(OscaErrorResponse::class)
    fun deleteInfoBoxMessage(
        @Query("msgId") msgId: Int,
        @Query("delete") delete: Boolean,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "DELETE_InfoBox"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun getAppointments(
        @Query("userId") userId: String,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_UserAppt"
    ): Maybe<OscaResponse<List<Appointment>>>


    @ErrorType(OscaErrorResponse::class)
    fun deleteAppointments(
        @Query("isDelete") isDelete: Boolean,
        @Query("apptId") apptId: String,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "PUT_UserAppt"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun cancelAppointments(
        @Query("id") apptId: String,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "PUT_CancelUserAppt"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun getWasteCalendar(
        @Body wasteCalendarRequest: WasteCalendarRequest,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "POST_WasteCalendarData"
    ): Maybe<OscaResponse<WasteCalendarResponse>>


    @ErrorType(OscaErrorResponse::class)
    fun saveWasteCalendarReminder(
        @Body request: WasteCalendarReminder,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "POST_WasteCalendarReminder"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun markAppointmentsAsRead(
        @Query("apptIds") apptIds: String,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "PUT_ApptReadStatus"
    ): Completable


    fun getSurveys(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_CitySurvey"
    ): Maybe<OscaResponse<List<Survey>>>


    fun getSurveyQuestions(
        @Query("surveyId") surveyId: Int,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_SurveyDetails"
    ): Maybe<OscaResponse<SurveyQuestions>>


    @ErrorType(OscaErrorResponse::class)
    fun submitSurvey(
        @Query("cityId") cityId: Int,
        @Query("surveyId") surveyId: Int,
        @Body submitSurvey: SubmitSurveyRequest,
        @Query("actionName") actionName: String = "POST_UserSurvey",
    ): Maybe<OscaResponse<SubmitResponse>>


    @ErrorType(OscaErrorResponse::class)
    fun acceptDataSecurityChanges(
        @Query("dpnAccepted") dpnAccepted: Boolean,
        @Query("cityId") cityId: Int = BuildConfig.CITY_ID,
        @Query("actionName") actionName: String = "PUT_UserDpnStatus"
    ): Completable


    @ErrorType(OscaErrorResponse::class)
    fun saveSelectedWastePickups(
        @Body saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest,
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "PUT_WasteTypeForUser"
    ): Maybe<OscaResponse<SaveSelectedWastePickupsResponse>>


    @ErrorType(OscaErrorResponse::class)
    fun getSelectedWastePickups(
        @Query("cityId") cityId: Int,
        @Query("actionName") actionName: String = "GET_UserWasteType"
    ): Maybe<OscaResponse<GetSelectedWastePickupsResponse>>

}
