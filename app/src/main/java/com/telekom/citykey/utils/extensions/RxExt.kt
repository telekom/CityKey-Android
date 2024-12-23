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
