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

package com.telekom.citykey.common

object ErrorCodes {
    const val INVALID_CREDENTIALS = "user.active"
    const val USER_BLOCKED_TEMP = "user.blocked.temp"
    const val USER_404 = "user.not.exist"
    const val USER_EXPIRED = "user.expired"
    const val USER_OFFLINE = "user.offline.account"
    const val USER_STATUS_UNKNOWN = " user.status.notknown"

    const val LOGIN_EMAIL_NOT_CONFIRMED = "email.not.verified"
    const val EMAIL_RESEND_SOON = "resend.too.soon"

    const val CHANGE_PASSWORD_OLD_PASSWORD_WRONG = "oldPwd.incorrect"
    const val CHANGE_PASSWORD_SAME_EMAIL = "newPwd.email.equal"
    const val PASSWORD_FORMAT_ERROR = "newPwd.not.valid"

    const val CHANGE_POSTAL_CODE_INVALID = "user.postal.code.invalid"
    const val CHANGE_POSTAL_CODE_VALIDATION_ERROR = "postalCode.validation.error"

    const val REGISTRATION_DATE_INVALID = "user.dob.not.valid"
    const val REGISTRATION_USER_TOO_YOUNG = "user.too.young"
    const val REGISTRATION_EMAIL_INVALID = "user.email.not.valid"
    const val REGISTRATION_EMAIL_EXIST = "user.email.exists"
    const val REGISTRATION_PASSWORD_INVALID = "pwd.validation.error"
    const val REGISTRATION_POSTALCODE_INVALID = "postalCode.validation.error"
    const val REGISTRATION_EMAIL_NOT_VERIFIED = "user.email.not.verified"

    const val FORGOT_PASSWORD_EMAIL_NOT_VERIFIED = "email.not.verified"
    const val FORGOT_PASSWORD_EMAIL_NOT_EXIST = "email.no.exist"
    const val FORGOT_PASSWORD_PASSWORD_INVALID = "password.not.valid"
    const val FORGOT_PASSWORD_EMAIL_INVALID = "email.not.valid"

    const val EMAIL_ALREADY_USED = "user.email.exists"
    const val EMAIL_INVALID = "user.email.not.valid"
    const val EMAIL_EMPTY = "error.email.empty"
    const val EMAIL_EQUALS = "old.email.equal"
    const val EMAIL_NO_EXIST = "user.no.exists"
    const val ACCOUNT_WRONG_PASSWORD = "delete.password.wrong"

    const val MULTIPLE_ERRORS = "form.validation.error"

    const val PROFILE_404 = "profile.not.found"

    const val WASTE_CALENDAR_NO_ADDRESS = "waste.address.not.exists"
    const val WASTE_CALENDAR_NOT_FOUND = "waste.calendar.not.found"
    const val WASTE_CALENDAR_WRONG_ADDRESS = "waste.address.wrong"
    const val CALENDAR_NOT_EXIST = "calendar.not.exist"

    const val DEFECT_OUTSIDE_CITY = "defect_outside_city"
    const val DEFECT_ALREADY_REPORTED = "defect_already_reported"
    const val DEFECT_IMAGE_TOO_LARGE_REPORTED = "file.too.large"
    const val DEFECT_NOT_FOUND = "defect.not.found"
    const val MULTIPLE_DEFECT_ALREADY_REPORTED = "multiple.defect.already.reported"
    const val DUPLICATE_DEFECT = "duplicate.defect"

    const val SERVICE_NOT_ACTIVE = "service.inactive"
    const val VERSION_NOT_SUPPORTED = "version.not.supported"

    const val ACTION_NOT_AVAILABLE = "action.not.available.for.city"
}
