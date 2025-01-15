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

import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaResponse
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import org.reactivestreams.Subscription

/*
    To be used for OSCA Responses(API Calls)
*/
inline fun <reified T : OscaResponse<R>, reified R> Maybe<T>.retryOnApiError(
    noinline onErrorAction: ((Throwable) -> Unit),
    retryDispatcher: Flowable<String>,
    pendingRetryCalls: MutableMap<String, Subscription?>
): Maybe<T> {
    return retryWhen { flow ->
        flow.switchMap { throwable ->
            if (throwable is NoConnectionException) {
                val callTag = R::class.java.name
                onErrorAction(throwable)
                retryDispatcher
                    .doOnSubscribe { pendingRetryCalls[callTag] = it }
                    .filter { tag -> tag == callTag }
            } else Flowable.error(throwable)
        }
    }
}

/*
    To be used for custom Maybe's.
 */
inline fun <reified T> Maybe<T>.retryOnError(
    noinline onErrorAction: (Throwable) -> Unit,
    retryDispatcher: Flowable<String>,
    pendingRetryCalls: MutableMap<String, Subscription?>
): Maybe<T> {
    return retryWhen { flow ->
        flow.switchMap { throwable ->
            if (throwable is NoConnectionException) {
                val callTag = T::class.java.name
                onErrorAction(throwable)
                retryDispatcher
                    .doOnSubscribe { pendingRetryCalls[callTag] = it }
                    .filter { tag -> tag == callTag }
            } else Flowable.error(throwable)
        }
    }
}

fun Completable.retryOnError(
    onErrorAction: ((Throwable) -> Unit),
    retryDispatcher: Flowable<String>,
    pendingRetryCalls: MutableMap<String, Subscription?>,
    callTag: String
): Completable {
    return retryWhen { flow ->
        flow.switchMap { throwable ->
            if (throwable is NoConnectionException) {
                onErrorAction(throwable)
                retryDispatcher
                    .doOnSubscribe { pendingRetryCalls[callTag] = it }
                    .filter { tag -> tag == callTag }
            } else
                Flowable.error(throwable)
        }
    }
}
