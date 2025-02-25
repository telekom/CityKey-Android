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

import android.content.Intent
import android.os.Bundle
import java.util.*


fun Intent?.intentToString(): String? {
    if (this == null) {
        return null
    }

    var out = "\n" + this.toString()
    val extras = this.extras
    if (extras != null) {
        extras.size()
        out += "\n${bundleToString(extras)}"
    }

    if (this.action != null) out += "\nAction = ${this.action}"

    if (this.type != null) out += "\nType = ${this.type}"

    if (this.data != null) out += "\nData = ${this.data}"

    if (this.getPackage() != null) out += "\nPackage = ${this.getPackage()}"

    if (this.dataString != null) out += "\nDataString = ${this.dataString}"

    return out
}

fun bundleToString(bundle: Bundle?): String {
    val out = StringBuilder("Bundle[")
    if (bundle == null) {
        out.append("null")
    } else {
        var first = true
        for (key in bundle.keySet()) {
            if (!first) {
                out.append(", ")
            }
            out.append(key).append('=')
            val value = bundle[key]
            if (value is IntArray) {
                out.append(Arrays.toString(value as IntArray?))
            } else if (value is ByteArray) {
                out.append(Arrays.toString(value as ByteArray?))
            } else if (value is BooleanArray) {
                out.append(Arrays.toString(value as BooleanArray?))
            } else if (value is ShortArray) {
                out.append(Arrays.toString(value as ShortArray?))
            } else if (value is LongArray) {
                out.append(Arrays.toString(value as LongArray?))
            } else if (value is FloatArray) {
                out.append(Arrays.toString(value as FloatArray?))
            } else if (value is DoubleArray) {
                out.append(Arrays.toString(value as DoubleArray?))
            } else if (value is Array<*>) {
                out.append(Arrays.toString(value as Array<*>?))
            } else if (value is Bundle) {
                out.append(bundleToString(value as Bundle?))
            } else {
                out.append(value)
            }
            first = false
        }
    }
    out.append("]")
    return out.toString()
}
