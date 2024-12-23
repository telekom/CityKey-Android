package com.telekom.citykey.custom.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProgressButtonViewBinding
import com.telekom.citykey.utils.extensions.dpToPixel

class ProgressButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val binding: ProgressButtonViewBinding =
        ProgressButtonViewBinding.inflate(LayoutInflater.from(context))

    var text: String? = null
        set(value) {
            field = value
            binding.button.text = value
        }

    val button: Button get() = binding.button

    val isLoading: Boolean get() = binding.progressBar.isVisible

    init {
        addView(binding.root)
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)
        text = typeArray.getString(R.styleable.ProgressButton_text)
        typeArray.recycle()
    }

    fun setText(@StringRes resId: Int) {
        text = context.getString(resId)
    }

    fun startLoading() {
        isEnabled = false
        binding.button.text = null
        binding.progressBar.visibility = View.VISIBLE
        hideKeyboard()
    }

    fun stopLoading() {
        isEnabled = true
        binding.button.text = text
        binding.progressBar.visibility = View.INVISIBLE
    }

    fun stopLoadingAfterError() {
        binding.button.text = text
        binding.progressBar.visibility = View.INVISIBLE
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setupOutlineStyle(@ColorInt color: Int = context.getColor(R.color.oscaColor)) {
        binding.button.strokeColor = ColorStateList.valueOf(color)
        binding.button.strokeWidth = 1.dpToPixel(context)
        binding.button.setBackgroundColor(context.getColor(R.color.background))
        binding.button.setTextColor(color)
        binding.progressBar.setColor(color)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setupNormalStyle(@ColorInt color: Int = context.getColor(R.color.oscaColor)) {
        val whiteColor = context.getColor(R.color.white)
        binding.button.strokeWidth = 0
        binding.button.setBackgroundColor(color)
        binding.button.setTextColor(whiteColor)
        binding.progressBar.setColor(whiteColor)
    }

    private fun hideKeyboard() {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Set this [button]'s width to [LayoutParams.MATCH_PARENT], as needed by some Landscape screens
     */
    fun setButtonWidthMatchParent() {
        button.post {
            val params = button.layoutParams as LayoutParams
            params.width = LayoutParams.MATCH_PARENT
            button.layoutParams = params
        }
    }

    /**
     * Set this [button]'s width to [LayoutParams.WRAP_CONTENT], as needed by some screens
     */
    fun setButtonWidthWrapContent() {
        button.post {
            val params = button.layoutParams as LayoutParams
            params.width = LayoutParams.WRAP_CONTENT
            button.layoutParams = params
        }
    }
}
