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

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

val View.isNotVisible: Boolean get() = visibility == View.GONE || visibility == View.INVISIBLE

fun View.setVisible(visible: Boolean, mode: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else mode
}

fun View.disable() {
    isEnabled = false
    animate().alpha(0.3f).setDuration(200).start()
}

fun View.enable() {
    isEnabled = true
    animate().alpha(1f).setDuration(200).start()
}

fun View.setAllEnabled(enabled: Boolean) {
    isEnabled = enabled
    if (this is ViewGroup) children.forEach { child -> child.setAllEnabled(enabled) }
}
