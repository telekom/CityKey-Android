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

package com.telekom.citykey.domain.whats_new

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.utils.PreferencesHelper

class WhatsNewInteractor(private val preferencesHelper: PreferencesHelper) {
    companion object {
        //update version name for production build
        const val WIDGET_RELEASE_VERSION_NAME = "1.3.5"
    }

    fun shouldShowWhatsNew() =
        preferencesHelper.isWhatsNewsScreenShown.not() && preferencesHelper.isFirstTime.not() && hasWhatsNewContent()

    private fun hasWhatsNewContent() =
        BuildConfig.APP_VERSION == WIDGET_RELEASE_VERSION_NAME

    fun whatsNewShown() {
        preferencesHelper.isWhatsNewsScreenShown = true
    }
}
