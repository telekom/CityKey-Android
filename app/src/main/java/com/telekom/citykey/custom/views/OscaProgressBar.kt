package com.telekom.citykey.custom.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.telekom.citykey.R

class OscaProgressBar(context: Context, attrs: AttributeSet) : ProgressBar(context, attrs) {

    private var isCustomSpinner = false

    init {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
            isCustomSpinner = true
            setupAnimation()
        }
    }

    private fun setupAnimation() {
        val avd = AnimatedVectorDrawableCompat.create(context, R.drawable.processing_loop_white)
        avd?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable) {
                post {
                    clearAnimation()
                    avd.start()
                }
            }
        })
        clearAnimation()
        indeterminateDrawable = avd
        avd?.start()
    }

    fun setColor(@ColorInt color: Int) {
        if (isCustomSpinner)
            (indeterminateDrawable as AnimatedVectorDrawableCompat)
                .colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        else
            indeterminateDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}
