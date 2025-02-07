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

package com.telekom.citykey.domain.ausweiss_app

import com.telekom.citykey.R
import com.telekom.citykey.domain.ausweiss_app.models.CertificateInfo
import com.telekom.citykey.domain.ausweiss_app.models.CertificateValidity
import com.telekom.citykey.domain.ausweiss_app.models.Result

@Suppress("ClassName")
sealed class IdentState {
    object INSERT_PUK : IdentState()
    object INSERT_CAN : IdentState()
    object ATTACH_CARD : IdentState()
    object LOADING : IdentState()
    object CARD_BLOCKED : IdentState()

    class InsertPin(val retries: Int) : IdentState()
    class Error(val result: Result?) : IdentState()

    class ShowInfo(
        accessRights: List<String>,
        val certificateInfo: CertificateInfo,
        val certificateValidity: CertificateValidity
    ) : IdentState() {

        val mappedAccessRights: List<Int> = accessRights.map {
            when (it) {
                IdentConst.AR_ADDRESS -> R.string.egov_sdk_Address
                IdentConst.AR_ADDRESS_VERIFICATION -> R.string.egov_sdk_AddressVerification
                IdentConst.AR_AGE_VERIFICATION -> R.string.egov_sdk_AgeVerification
                IdentConst.AR_ARTISTIC_NAME -> R.string.egov_sdk_ArtisticName
                IdentConst.AR_BIRTH_NAME -> R.string.egov_sdk_BirthName
                IdentConst.AR_CAN_ALLOWED -> R.string.egov_sdk_CanAllowed
                IdentConst.AR_COMMUNITY_ID -> R.string.egov_sdk_CommunityID
                IdentConst.AR_DATE_OF_BIRTH -> R.string.egov_sdk_DateOfBirth
                IdentConst.AR_DOCTORAL_DEGREE -> R.string.egov_sdk_DoctoralDegree
                IdentConst.AR_DOCUMENT_TYPE -> R.string.egov_sdk_DocumentType
                IdentConst.AR_FAMILY_NAME -> R.string.egov_sdk_FamilyName
                IdentConst.AR_GIVEN_NAMES -> R.string.egov_sdk_GivenNames
                IdentConst.AR_ISSUING_COUNTRY -> R.string.egov_sdk_IssuingCountry
                IdentConst.AR_NATIONALITY -> R.string.egov_sdk_Nationality
                IdentConst.AR_PIN_MANAGEMENT -> R.string.egov_sdk_PinManagement
                IdentConst.AR_PLACE_OF_BIRTH -> R.string.egov_sdk_PlaceOfBirth
                IdentConst.AR_PSEUDONYM -> R.string.egov_sdk_Pseudonym
                IdentConst.AR_RESIDENCE_PERMITI -> R.string.egov_sdk_ResidencePermitI
                IdentConst.AR_RESIDENCE_PERMITII -> R.string.egov_sdk_ResidencePermitII
                IdentConst.AR_VALID_UNTIL -> R.string.egov_sdk_ValidUntil
                IdentConst.AR_WRITE_ADDRESS -> R.string.egov_sdk_Address
                IdentConst.AR_WRITE_COMMUNITY_ID -> R.string.egov_sdk_CommunityID
                IdentConst.AR_WRITE_RESIDENCE_PERMITI -> R.string.egov_sdk_ResidencePermitI
                else -> R.string.egov_sdk_WriteResidencePermitII
            }
        }
    }

    class Success(val url: String) : IdentState()
}
