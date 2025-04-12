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

package com.telekom.citykey.domain.user.smartlock

import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialsClient
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CredentialsClientHandler(private val credentialsClient: CredentialsClient) {

    companion object {
        private const val APP_TOKEN = "https://osca.all-ip.t-online.de/"
        const val CHOOSE_MULTIPLE = 3001
        const val ASK_FOR_SAVE = 3002
    }

    fun retrieveCredentials(): Observable<Credential> =
        Observable.create { emitter ->
            credentialsClient.request(
                CredentialRequest.Builder()
                    .setPasswordLoginSupported(true)
                    .setServerClientId(APP_TOKEN)
                    .setAccountTypes(APP_TOKEN)
                    .build()
            )
                .addOnSuccessListener { response ->
                    response.credential?.let(emitter::onNext)
                }
                .addOnFailureListener {
                    if (it is ResolvableApiException && it.statusCode != CommonStatusCodes.SIGN_IN_REQUIRED) {
                        emitter.onError(ResolvableException(it, CHOOSE_MULTIPLE))
                    } else Timber.e(it)
                }
        }

    fun saveCredentials(email: String, password: String): Completable =
        Completable.create { emitter ->
            credentialsClient
                .save(Credential.Builder(email).setPassword(password).build())
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(ResolvableException(it, ASK_FOR_SAVE)) }
        }
            .subscribeOn(Schedulers.io())
            .timeout(10L, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
}
