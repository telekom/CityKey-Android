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

package com.telekom.citykey.domain.repository

import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.domain.repository.exceptions.UnsupportedVersionException
import com.telekom.citykey.models.api.requests.FeedbackRequest
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OscaRepository(
    private val api: SmartCityApi,
    private val authApi: SmartCityAuthApi
) {

    fun setMailRead(msgId: Int, markRead: Boolean) = authApi.setInformationRead(msgId, markRead)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getLegalData() = api.getLegalData()
        .subscribeOn(Schedulers.io())
        .map { it.content[0] }
        .observeOn(AndroidSchedulers.mainThread())

    fun getMailBox() = authApi.getInfoBox()
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun deleteMail(msgId: Int, delete: Boolean) = authApi.deleteInfoBoxMessage(msgId, delete)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun acceptDataSecurityChanges(dpnAccepted: Boolean) = authApi.acceptDataSecurityChanges(dpnAccepted)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun sendFeedback(feedbackRequest: FeedbackRequest) = api.sendFeedback(feedbackRequest)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun checkAppVersion() = api.checkAppVersion()
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
