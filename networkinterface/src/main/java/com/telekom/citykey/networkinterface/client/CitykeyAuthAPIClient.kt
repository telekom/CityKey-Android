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
import com.telekom.citykey.networkinterface.models.api.requests.DeleteAccountRequest
import com.telekom.citykey.networkinterface.models.api.requests.EmailChangeRequest
import com.telekom.citykey.networkinterface.models.api.requests.PasswordChangeRequest
import com.telekom.citykey.networkinterface.models.api.requests.PersonalDetailChangeRequest
import com.telekom.citykey.networkinterface.models.api.requests.SubmitSurveyRequest
import com.telekom.citykey.networkinterface.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.networkinterface.models.appointments.Appointment
import com.telekom.citykey.networkinterface.models.citizen_survey.SubmitResponse
import com.telekom.citykey.networkinterface.models.citizen_survey.Survey
import com.telekom.citykey.networkinterface.models.citizen_survey.SurveyQuestions
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.networkinterface.models.content.UserProfile
import com.telekom.citykey.networkinterface.models.user.InfoBoxContent
import com.telekom.citykey.networkinterface.models.user.ResidenceValidationResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.GetSelectedWastePickupsResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.SaveSelectedWastePickupRequest
import com.telekom.citykey.networkinterface.models.waste_calendar.SaveSelectedWastePickupsResponse
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarReminder
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteCalendarResponse
import io.reactivex.Completable
import io.reactivex.Maybe

interface CitykeyAuthAPIClient {

    fun setInformationRead(msgId: Int, markRead: Boolean, cityId: Int): Completable

    fun getUserProfile(cityId: Int): Maybe<OscaResponse<UserProfile>>

    fun changePassword(passwordReset: PasswordChangeRequest, cityId: Int): Completable

    fun changeEmail(request: EmailChangeRequest, cityId: Int): Completable

    fun changePersonalData(request: PersonalDetailChangeRequest, update: String, cityId: Int): Completable

    fun validatePostalCode(postalCode: String, cityId: Int): Maybe<OscaResponse<ResidenceValidationResponse>>

    fun deleteUser(request: DeleteAccountRequest, cityId: Int): Completable

    fun setEventFavored(markFavorite: Boolean, eventId: Long, cityId: Int): Completable

    fun getFavoredEvents(cityId: Int): Maybe<OscaResponse<List<Event>>>

    fun getInfoBox(cityId: Int): Maybe<OscaResponse<MutableList<InfoBoxContent>>>

    fun deleteInfoBoxMessage(msgId: Int, delete: Boolean, cityId: Int): Completable

    fun getAppointments(userId: String, cityId: Int): Maybe<OscaResponse<List<Appointment>>>

    fun deleteAppointments(
        isDelete: Boolean,
        apptId: String,
        cityId: Int
    ): Completable

    fun cancelAppointments(apptId: String, cityId: Int): Completable

    fun getWasteCalendar(
        wasteCalendarRequest: WasteCalendarRequest,
        cityId: Int
    ): Maybe<OscaResponse<WasteCalendarResponse>>

    fun saveWasteCalendarReminder(request: WasteCalendarReminder, cityId: Int): Completable

    fun markAppointmentsAsRead(apptIds: String, cityId: Int): Completable

    fun getSurveys(cityId: Int): Maybe<OscaResponse<List<Survey>>>

    fun getSurveyQuestions(surveyId: Int, cityId: Int): Maybe<OscaResponse<SurveyQuestions>>

    fun submitSurvey(cityId: Int, surveyId: Int, submitSurvey: SubmitSurveyRequest): Maybe<OscaResponse<SubmitResponse>>

    fun acceptDataSecurityChanges(dpnAccepted: Boolean, cityId: Int): Completable

    fun saveSelectedWastePickups(
        saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest,
        cityId: Int
    ): Maybe<OscaResponse<SaveSelectedWastePickupsResponse>>

    fun getSelectedWastePickups(cityId: Int): Maybe<OscaResponse<GetSelectedWastePickupsResponse>>
}
