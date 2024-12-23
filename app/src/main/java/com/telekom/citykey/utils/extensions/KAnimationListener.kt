package com.telekom.citykey.utils.extensions

import android.view.animation.*

inline fun Animation.listen(init: KAnimationListener.() -> Unit) =
    setAnimationListener(KAnimationListener().apply(init))

class KAnimationListener : Animation.AnimationListener {
    private var _onAnimationRepeat: ((Animation) -> Unit)? = null
    private var _onAnimationEnd: ((Animation) -> Unit)? = null
    private var _onAnimationStart: ((Animation) -> Unit)? = null

    override fun onAnimationRepeat(animation: Animation) {
        _onAnimationRepeat?.invoke(animation)
    }

    override fun onAnimationEnd(animation: Animation) {
        _onAnimationEnd?.invoke(animation)
    }

    override fun onAnimationStart(animation: Animation) {
        _onAnimationStart?.invoke(animation)
    }

    fun onAnimationRepeat(listener: (Animation) -> Unit) {
        _onAnimationRepeat = listener
    }

    fun onAnimationEnd(listener: (Animation) -> Unit) {
        _onAnimationEnd = listener
    }

    fun onAnimationStart(listener: (Animation) -> Unit) {
        _onAnimationStart = listener
    }
}
