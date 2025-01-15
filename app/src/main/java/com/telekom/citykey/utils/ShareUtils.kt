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

package com.telekom.citykey.utils

import android.content.Intent

object ShareUtils {
    fun createShareIntent(title: String, url: String, shareHeader: String): Intent {
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, createText(title, url, shareHeader))
                putExtra(Intent.EXTRA_TITLE, title) // relevant for >= Android 10
                putExtra(Intent.EXTRA_SUBJECT, title)
                type = "text/plain"
            },
            null
        )
    }

    private fun createText(title: String, url: String, shareHeader: String) = StringBuilder()
        .appendLine(title)
        .appendLine(url)
        .appendLine()
        .appendLine(shareHeader)
        .toString()
}
