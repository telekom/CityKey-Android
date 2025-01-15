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
