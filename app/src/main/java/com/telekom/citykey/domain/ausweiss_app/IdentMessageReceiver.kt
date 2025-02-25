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

package com.telekom.citykey.domain.ausweiss_app

import android.os.RemoteException
import com.google.gson.Gson
import com.governikus.ausweisapp2.IAusweisApp2SdkCallback
import com.telekom.citykey.R
import com.telekom.citykey.domain.ausweiss_app.models.IdentMessage
import com.telekom.citykey.domain.track.AdjustManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class IdentMessageReceiver(val responseHandler: (IdentMsg) -> Unit) : IAusweisApp2SdkCallback.Stub(), KoinComponent {
    var mSessionID: String? = null
    var isAuthenticationStarted = false

    private val adjustManager: AdjustManager by inject()

    @Throws(RemoteException::class)
    override fun sessionIdGenerated(
        pSessionId: String,
        pIsSecureSessionId: Boolean
    ) {
        mSessionID = pSessionId
        isAuthenticationStarted = false
    }

    @Throws(RemoteException::class)
    override fun receive(pJson: String) {
        createResponse(pJson)
    }

    @Throws(RemoteException::class)
    override fun sdkDisconnected() {
        Timber.i("SDK Disconnected")
    }

    private fun createResponse(messageJson: String) {
        // Workaround for extracting the url from the messageJson.
        Timber.i(messageJson)

        val message: IdentMessage? = try {
            Gson().fromJson(messageJson, IdentMessage::class.java)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }

        if (message == null) {
            adjustManager.run {
                trackEvent(R.string.eid_authentication_failed_message_error)
                trackEvent(R.string.eid_authentication_failed)
            }
            return
        }

        when (message.msg) {

            IdentConst.MSG_AUTH -> {
                if (message.error != null && message.error is String) {
                    adjustManager.run {
                        trackEvent(R.string.eid_authentication_failed_payload_error)
                        trackEvent(R.string.eid_authentication_failed)
                    }
                    return
                } else if (message.result == null && message.url.isNullOrBlank()) {
                    // Authentication Started
                    isAuthenticationStarted = true
                    adjustManager.trackEvent(R.string.eid_process_started)
                } else if (message.result?.major?.contains("#error") == true) {
                    responseHandler(IdentMsg.ErrorResult(message.result))
                    adjustManager.run {
                        trackEvent(R.string.eid_authentication_failed_major_error)
                        trackEvent(R.string.eid_authentication_failed)
                    }
                } else if (!message.url.isNullOrBlank()) {
                    responseHandler(IdentMsg.Completed(message.url))
                } else {
                    responseHandler(IdentMsg.Error("unknown"))
                }
            }

            IdentConst.MSG_ACCESS_RIGHTS -> {
                if (isAuthenticationStarted) {
                    responseHandler(IdentMsg.AccessRights(message.chat.effective))
                } else {
                    adjustManager.run {
                        trackEvent(R.string.eid_authentication_failed_access_rights_error)
                        trackEvent(R.string.eid_authentication_failed)
                    }
                }
            }

            IdentConst.MSG_INSERT_CARD -> responseHandler(IdentMsg.INSERT_CARD)
            IdentConst.MSG_ENTER_PIN -> {
                val retries = message.reader?.card?.retryCounter ?: 3
                responseHandler(IdentMsg.RequestPin(retries))
            }

            IdentConst.MSG_ENTER_PUK -> responseHandler(IdentMsg.REQUEST_PUK)
            IdentConst.MSG_ENTER_CAN -> responseHandler(IdentMsg.REQUEST_CAN)
            IdentConst.MSG_INSERT_CERTIFICATE -> responseHandler(
                IdentMsg.Certificate(
                    message.description,
                    message.validity
                )
            )

            IdentConst.MSG_BAD_STATE -> {
                adjustManager.run {
                    trackEvent(R.string.eid_authentication_failed_bad_state_error)
                    trackEvent(R.string.eid_authentication_failed)
                }
                responseHandler(IdentMsg.Error(messageJson))
            }

            IdentConst.MSG_READER -> {
                if (message.card?.inoperative == true) {
                    responseHandler(IdentMsg.CARD_BLOCKED)
                } else {
                    responseHandler(IdentMsg.CardRecognized(message.card))
                }
            }

            IdentConst.MSG_STATUS -> {
                // This is the status of progress of current command execution. Do Nothing.
            }

            else -> {
                adjustManager.run {
                    trackEvent(R.string.eid_authentication_failed_message_error)
                    trackEvent(R.string.eid_authentication_failed)
                }
                responseHandler(IdentMsg.Error("Unhandled messageJson $message"))
            }

        }
    }
}
