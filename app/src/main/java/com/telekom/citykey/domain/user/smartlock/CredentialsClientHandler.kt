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
