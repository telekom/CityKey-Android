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

package com.telekom.citykey.domain.repository

import com.telekom.citykey.BuildConfig
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.data.exceptions.UnsupportedVersionException
import com.telekom.citykey.networkinterface.client.CitykeyAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyAuthAPIClient
import com.telekom.citykey.networkinterface.models.api.requests.FeedbackRequest
import com.telekom.citykey.networkinterface.models.content.Terms
import com.telekom.citykey.networkinterface.models.user.InfoBoxContent
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OscaRepository(
    private val api: CitykeyAPIClient,
    private val authApi: CitykeyAuthAPIClient,
) {

    fun setMailRead(msgId: Int, markRead: Boolean): Completable = authApi.setInformationRead(
        msgId = msgId,
        markRead = markRead,
        cityId = BuildConfig.CITY_ID
    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun getLegalData(): Maybe<Terms> = api.getLegalData(cityId = BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .map { it.content[0] }
        .observeOn(AndroidSchedulers.mainThread())

    fun getMailBox(): Maybe<MutableList<InfoBoxContent>> = authApi.getInfoBox(cityId = BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun deleteMail(msgId: Int, delete: Boolean): Completable = authApi.deleteInfoBoxMessage(
        msgId = msgId,
        delete = delete,
        cityId = BuildConfig.CITY_ID
    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun acceptDataSecurityChanges(
        dpnAccepted: Boolean
    ): Completable = authApi.acceptDataSecurityChanges(dpnAccepted = dpnAccepted, cityId = BuildConfig.CITY_ID)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun sendFeedback(feedbackRequest: FeedbackRequest): Completable =
        api.sendFeedback(feedbackRequest, cityId = BuildConfig.CITY_ID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun checkAppVersion(): Completable =
        api.checkAppVersion(version = BuildConfig.APP_VERSION, cityId = BuildConfig.CITY_ID)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { response ->
                if (response.content.any { it.errorCode == ErrorCodes.VERSION_NOT_SUPPORTED })
                    Completable.error(UnsupportedVersionException())
                else
                    Completable.complete()
            }
            .onErrorResumeNext { error ->
                if (error is UnsupportedVersionException) Completable.error(error)
                else Completable.complete()
            }
            .observeOn(AndroidSchedulers.mainThread())
}
