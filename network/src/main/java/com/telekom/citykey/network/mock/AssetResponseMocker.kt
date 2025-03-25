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

package com.telekom.citykey.network.mock

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.telekom.citykey.networkinterface.models.OscaResponse

/**
 * A helper utility to mock the responses as taken from JSON file stored in Assets
 *
 * @param context Context provided by Koin
 * @param gson Gson object used to parse the Asset JSON
 */
internal class AssetResponseMocker(
    val context: Context,
    val gson: Gson
) {

    /**
     * @param fileName Name of the JSON file to take the data from
     * @return the data of type [T], parsed from Assets JSON file with name "[fileName].json", wrapped in [OscaResponse]
     */
    inline fun <reified T> getOscaResponseOf(
        fileName: String
    ): OscaResponse<T> {

        val fileNameWithExtension = "$fileName.json"

        val oscaResponseType = TypeToken.getParameterized(OscaResponse::class.java, T::class.java)

        val response: OscaResponse<T> = gson.fromJson(
            context.assets.getJsonContent(fileNameWithExtension),
            oscaResponseType.type
        )

        return response
    }

    /**
     * @param fileName Name of the JSON file to take the data from
     * @return the data of type [List] of [T], parsed from Assets JSON file with name "[fileName].json", wrapped in [OscaResponse]
     */
    inline fun <reified T> getOscaResponseListOf(
        fileName: String,
    ): OscaResponse<List<T>> {

        val fileNameWithExtension = "$fileName.json"

        val listType = TypeToken.getParameterized(List::class.java, T::class.java)

        val oscaResponseType = TypeToken.getParameterized(OscaResponse::class.java, listType.type)

        val response: OscaResponse<List<T>> = gson.fromJson(
            context.assets.getJsonContent(fileNameWithExtension),
            oscaResponseType.type
        )

        return response
    }

    /**
     * A quick helper method to get the JSON content from the Assets file with given [fileName]
     *
     * @param fileName Name of the JSON file to take the data from
     */
    fun AssetManager.getJsonContent(fileName: String): String = open(fileName).bufferedReader().use { it.readText() }
}
