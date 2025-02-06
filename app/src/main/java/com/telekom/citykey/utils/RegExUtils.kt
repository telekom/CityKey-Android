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

package com.telekom.citykey.utils

import androidx.core.util.PatternsCompat
import java.util.regex.Pattern

object RegExUtils {
    private const val HTML_A_TAG_REG_EX = "(?i)<a([^>]+)>(.+?)</a>"
    private const val HTML_A_HREF_TAG_REG_EX = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))"

    private const val PHONE_NUMBER = "((\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4})" +
            "|((\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3})" +
            "|((\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2})" +
            "|((0\\d{3,4}[- .]?)?\\d{3}[- .]?\\d{3})"
    // Valid Phone Numbers = {"2055550125","202 555 0125", "(202) 555-0125", "+111 (202) 555-0125", "636 856 789",
    // "+111 636 856 789", "636 85 67 89", "+111 636 85 67 89", "02055 550 125"}

    private const val HEX_COLOR = "#(([0-9a-fA-F]{2}){3,4}|([0-9a-fA-F]){3,4})\\b"

    const val PHONE_URI_PREFIX = "tel:"
    const val EMAIL_URI_PREFIX = "mailto:"

    val anchorTag: Pattern by lazy { Pattern.compile(HTML_A_TAG_REG_EX) }
    val anchorTagHrefValue: Pattern by lazy { Pattern.compile(HTML_A_HREF_TAG_REG_EX) }
    val emailAddress: Pattern by lazy { PatternsCompat.EMAIL_ADDRESS }
    val webUrl: Pattern by lazy { PatternsCompat.WEB_URL }
    val phoneNumber: Pattern by lazy { Pattern.compile(PHONE_NUMBER) }
    val hexColor: Pattern by lazy { Pattern.compile(HEX_COLOR) }
}
