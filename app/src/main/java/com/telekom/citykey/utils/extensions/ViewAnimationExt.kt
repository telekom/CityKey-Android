package com.telekom.citykey.utils.extensions

import android.view.View
import androidx.core.view.ViewCompat
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject

fun View.fadeIn(duration: Long = 300): Completable {
    val animationSubject = CompletableSubject.create()
    return animationSubject.doOnSubscribe {
        ViewCompat.animate(this)
            .setDuration(duration)
            .alpha(1f)
            .withStartAction { visibility = View.VISIBLE }
            .withEndAction { animationSubject.onComplete() }
    }
}

fun View.fadeOut(duration: Long = 300, setGoneAfterwards: Boolean = true): Completable {
    val animationSubject = CompletableSubject.create()
    return animationSubject.doOnSubscribe {
        ViewCompat.animate(this)
            .setDuration(duration)
            .alpha(0f)
            .withStartAction { visibility = View.VISIBLE }
            .withEndAction {
                if (setGoneAfterwards) visibility = View.GONE
                animationSubject.onComplete()
            }
    }
}
