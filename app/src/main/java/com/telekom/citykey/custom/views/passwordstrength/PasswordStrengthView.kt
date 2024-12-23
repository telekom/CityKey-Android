package com.telekom.citykey.custom.views.passwordstrength

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.telekom.citykey.R

class PasswordStrengthView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    companion object {
        private const val BLACK_COLOR = R.color.onSurface
        private const val RED_COLOR = R.color.red
        private const val GREEN_COLOR = R.color.lima
    }

    private val strengthProgressBar: ProgressBar by lazy { findViewById(R.id.strengthProgressbar) }
    private val tvHints: TextView by lazy { findViewById(R.id.tvHints) }
    private val contentHeight: Int
    private var isItemShown = false
    private var heightAnimator: ValueAnimator? = null
    private val view: View = View.inflate(context, R.layout.registration_profile_custom_password_strength_view, null)
    private var lastValidation: PasswordStrength? = null

    init {
        view.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        contentHeight = view.measuredHeight

        addView(view)
    }

    fun toggleCollapsing() {
        heightAnimator?.cancel()

        val toHeight = if (isItemShown) 0 else contentHeight
        isItemShown = !isItemShown

        heightAnimator = ValueAnimator.ofInt(this@PasswordStrengthView.height, toHeight).apply {
            duration = 300
            addUpdateListener {
                this@PasswordStrengthView.layoutParams.height = it.animatedValue as Int
                this@PasswordStrengthView.requestLayout()
            }
            start()
        }
    }

    fun updateValidation(passwordStrength: PasswordStrength) {
        lastValidation = passwordStrength
        updateHints(passwordStrength.spannableStringBuilder)
        setProgress(passwordStrength.percentage)
    }

    fun initValidation(hints: String) {
        tvHints.text = hints
    }

    private fun setProgress(percentage: Int) {
        strengthProgressBar.progressTintList = ColorStateList.valueOf(
            context.getColor(
                when (percentage) {
                    0 -> BLACK_COLOR
                    100 -> GREEN_COLOR
                    else -> RED_COLOR
                }
            )
        )
        strengthProgressBar.progress = percentage
    }

    private fun updateHints(spannableString: SpannableStringBuilder) {
        tvHints.text = spannableString
    }
}
