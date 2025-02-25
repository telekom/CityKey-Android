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

package com.telekom.citykey.domain.ausweiss_app

object IdentConst {
    const val MSG_ACCESS_RIGHTS = "ACCESS_RIGHTS"
    const val MSG_ENTER_PIN = "ENTER_PIN"
    const val MSG_ENTER_PUK = "ENTER_PUK"
    const val MSG_ENTER_CAN = "ENTER_CAN"
    const val MSG_INSERT_CARD = "INSERT_CARD"
    const val MSG_INSERT_CERTIFICATE = "CERTIFICATE"
    const val MSG_BAD_STATE = "BAD_STATE"
    const val MSG_READER = "READER"
    const val MSG_CUSTOM_URL = "URL"
    const val MSG_AUTH = "AUTH"
    const val MSG_STATUS = "STATUS"
    const val CMD_RUN_AUTH = "RUN_AUTH"
    const val CMD_GET_CERTIFICATE = "GET_CERTIFICATE"
    const val CMD_ACCEPT = "ACCEPT"
    const val CMD_CANCEL = "CANCEL"
    const val CMD_GET_READER = "GET_READER_LIST"
    const val CMD_SET_PIN = "SET_PIN"
    const val CMD_SET_PUK = "SET_PUK"
    const val CMD_SET_CAN = "SET_CAN"
    const val PARAM_TCTOKEN = "tcTokenURL"
    const val PARAM_VALUE = "value"
    const val PARAM_CMD = "cmd"

    const val AR_ADDRESS = "Address"
    const val AR_ADDRESS_VERIFICATION = "AddressVerification"
    const val AR_AGE_VERIFICATION = "AgeVerification"
    const val AR_ARTISTIC_NAME = "ArtisticName"
    const val AR_BIRTH_NAME = "BirthName"
    const val AR_CAN_ALLOWED = "CanAllowed"
    const val AR_COMMUNITY_ID = "CommunityID"
    const val AR_DATE_OF_BIRTH = "DateOfBirth"
    const val AR_DOCTORAL_DEGREE = "DoctoralDegree"
    const val AR_DOCUMENT_TYPE = "DocumentType"
    const val AR_FAMILY_NAME = "FamilyName"
    const val AR_GIVEN_NAMES = "GivenNames"
    const val AR_ISSUING_COUNTRY = "IssuingCountry"
    const val AR_NATIONALITY = "Nationality"
    const val AR_PIN_MANAGEMENT = "PinManagement"
    const val AR_PLACE_OF_BIRTH = "PlaceOfBirth"
    const val AR_PSEUDONYM = "Pseudonym"
    const val AR_RESIDENCE_PERMITI = "ResidencePermitI"
    const val AR_RESIDENCE_PERMITII = "ResidencePermitII"
    const val AR_VALID_UNTIL = "ValidUntil"
    const val AR_WRITE_ADDRESS = "WriteAddress"
    const val AR_WRITE_COMMUNITY_ID = "WriteCommunityID"
    const val AR_WRITE_RESIDENCE_PERMITI = "WriteResidencePermitI"
    const val AR_WRITE_RESIDENCE_PERMITII = "WriteResidencePermitII"
}
