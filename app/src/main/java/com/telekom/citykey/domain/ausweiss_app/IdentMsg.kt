
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

package com.telekom.citykey.domain.ausweiss_app

import com.telekom.citykey.domain.ausweiss_app.models.Card
import com.telekom.citykey.domain.ausweiss_app.models.CertificateInfo
import com.telekom.citykey.domain.ausweiss_app.models.CertificateValidity
import com.telekom.citykey.domain.ausweiss_app.models.Result

@Suppress("ClassName")
sealed class IdentMsg {
    object REQUEST_PUK : IdentMsg()
    object REQUEST_CAN : IdentMsg()
    object INSERT_CARD : IdentMsg()
    object CARD_BLOCKED : IdentMsg()

    class RequestPin(val retries: Int) : IdentMsg()
    class Error(val msg: String) : IdentMsg()
    class ErrorResult(val result: Result) : IdentMsg()
    class AccessRights(val rights: List<String>) : IdentMsg()
    class Completed(val url: String) : IdentMsg()
    class Certificate(val certificateInfo: CertificateInfo, val certificateValidity: CertificateValidity) : IdentMsg()
    class CardRecognized(val card: Card?) : IdentMsg()
}
