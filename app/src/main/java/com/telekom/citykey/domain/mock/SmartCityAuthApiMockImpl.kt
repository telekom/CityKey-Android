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

package com.telekom.citykey.domain.mock

import com.telekom.citykey.domain.repository.SmartCityAuthApi
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.api.requests.DeleteAccountRequest
import com.telekom.citykey.models.api.requests.EmailChangeRequest
import com.telekom.citykey.models.api.requests.PasswordChangeRequest
import com.telekom.citykey.models.api.requests.PersonalDetailChangeRequest
import com.telekom.citykey.models.api.requests.SubmitSurveyRequest
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.models.appointments.Appointment
import com.telekom.citykey.models.citizen_survey.SubmitResponse
import com.telekom.citykey.models.citizen_survey.Survey
import com.telekom.citykey.models.citizen_survey.SurveyQuestions
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.models.user.InfoBoxContent
import com.telekom.citykey.models.user.ResidenceValidationResponse
import com.telekom.citykey.models.waste_calendar.GetSelectedWastePickupsResponse
import com.telekom.citykey.models.waste_calendar.SaveSelectedWastePickupRequest
import com.telekom.citykey.models.waste_calendar.SaveSelectedWastePickupsResponse
import com.telekom.citykey.models.waste_calendar.WasteCalendarReminder
import com.telekom.citykey.models.waste_calendar.WasteCalendarResponse
import io.reactivex.Completable
import io.reactivex.Maybe

private const val GET_USER_PROFILE = "get_user_profile"

private const val VALIDATE_POSTAL_CODE = "validate_postal_code"

private const val GET_FAVORED_EVENTS = "get_favored_events"

private const val GET_INFO_BOX = "get_info_box"

private const val GET_APPOINTMENTS = "get_appointments"

private const val GET_WASTE_CALENDAR = "get_waste_calendar"
private const val SAVE_SELECTED_WASTE_PICKUPS = "save_selected_waste_pickups"
private const val GET_SELECTED_WASTE_PICKUPS = "get_selected_waste_pickups"

private const val GET_SURVEYS = "get_surveys"
private const val GET_SURVEY_QUESTIONS = "get_survey_questions"
private const val SUBMIT_SURVEY = "submit_survey"

class SmartCityAuthApiMockImpl(
    private val assetResponseMocker: AssetResponseMocker
) : SmartCityAuthApi {

    override fun setInformationRead(
        msgId: Int,
        markRead: Boolean,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getUserProfile(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<UserProfile>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(GET_USER_PROFILE)
    )

    override fun changePassword(
        passwordReset: PasswordChangeRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun changeEmail(
        request: EmailChangeRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun changePersonalData(
        request: PersonalDetailChangeRequest,
        update: String,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun validatePostalCode(
        postalCode: String,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<ResidenceValidationResponse>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(VALIDATE_POSTAL_CODE)
    )

    override fun deleteUser(
        request: DeleteAccountRequest,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun setEventFavored(
        markFavorite: Boolean,
        eventId: Long,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getFavoredEvents(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<Event>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_FAVORED_EVENTS)
    )

    override fun getInfoBox(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<InfoBoxContent>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_INFO_BOX)
    )

    override fun deleteInfoBoxMessage(
        msgId: Int,
        delete: Boolean,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getAppointments(
        userId: String,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<Appointment>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_APPOINTMENTS)
    )

    override fun deleteAppointments(
        isDelete: Boolean,
        apptId: String,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun cancelAppointments(
        apptId: String,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getWasteCalendar(
        wasteCalendarRequest: WasteCalendarRequest,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<WasteCalendarResponse>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(GET_WASTE_CALENDAR)
    )

    override fun saveWasteCalendarReminder(
        request: WasteCalendarReminder,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun markAppointmentsAsRead(
        apptIds: String,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun getSurveys(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<List<Survey>>> = Maybe.just(
        assetResponseMocker.getOscaResponseListOf(GET_SURVEYS)
    )

    override fun getSurveyQuestions(
        surveyId: Int,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<SurveyQuestions>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(GET_SURVEY_QUESTIONS)
    )

    override fun submitSurvey(
        cityId: Int,
        surveyId: Int,
        submitSurvey: SubmitSurveyRequest,
        actionName: String
    ): Maybe<OscaResponse<SubmitResponse>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(SUBMIT_SURVEY)
    )

    override fun acceptDataSecurityChanges(
        dpnAccepted: Boolean,
        cityId: Int,
        actionName: String
    ): Completable = Completable.complete()

    override fun saveSelectedWastePickups(
        saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest,
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<SaveSelectedWastePickupsResponse>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(SAVE_SELECTED_WASTE_PICKUPS)
    )

    override fun getSelectedWastePickups(
        cityId: Int,
        actionName: String
    ): Maybe<OscaResponse<GetSelectedWastePickupsResponse>> = Maybe.just(
        assetResponseMocker.getOscaResponseOf(GET_SELECTED_WASTE_PICKUPS)
    )
}
