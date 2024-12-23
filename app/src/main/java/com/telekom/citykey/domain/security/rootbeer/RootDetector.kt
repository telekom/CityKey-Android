package com.telekom.citykey.domain.security.rootbeer

import com.scottyab.rootbeer.RootBeer
import com.telekom.citykey.BuildConfig
import io.reactivex.Completable
import io.reactivex.Single

class RootDetector(private val rootBeer: RootBeer) {

    val rootChecker
        get() = Single.fromCallable { rootBeer.isRooted }
            .flatMapCompletable { rootDetected ->
                return@flatMapCompletable if (rootDetected && BuildConfig.FLAVOR == "production") Completable.error(
                    RootDetectedException()
                )
                else Completable.complete()
            }
            .onErrorResumeNext {
                return@onErrorResumeNext if (it is RootDetectedException) Completable.error(
                    it
                )
                else Completable.complete()
            }
}
