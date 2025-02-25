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

package com.telekom.citykey.utils.extensions

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.util.LinkifyCompat
import com.telekom.citykey.domain.city.CityInteractor

fun AppCompatTextView.autoLinkAll(@ColorInt color: Int = CityInteractor.cityColorInt) {
    setLinkTextColor(color)
    LinkifyCompat.addLinks(this, Linkify.EMAIL_ADDRESSES and Linkify.WEB_URLS)

//    LinkifyCompat.addLinks(
//        this,
//        Patterns.PHONE,
//        "tel:",
//        { chars, _, _ -> chars.map { it.isDigit() }.size >= 6 },
//        Linkify.sPhoneNumberTransformFilter
//    )

    movementMethod = LinkMovementMethod.getInstance()
}

fun AppCompatTextView.linkify(text: CharSequence, @ColorInt color: Int = CityInteractor.cityColorInt) {
    setLinkTextColor(color)

    val currentSpans = (text as? Spanned)?.getSpans(0, text.length, URLSpan::class.java)

    val buffer = SpannableString(text)
    Linkify.addLinks(buffer, Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)

    currentSpans?.forEach { span ->
        val end = text.getSpanEnd(span)
        val start = text.getSpanStart(span)
        buffer.setSpan(span, start, end, 0)
    }

    this.text = buffer

    movementMethod = LinkMovementMethod.getInstance()
}
