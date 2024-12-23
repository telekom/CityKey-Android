package com.telekom.citykey.custom.views.inputfields

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AuthInputPadBinding

class AuthInputPad @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val binding: AuthInputPadBinding =
        AuthInputPadBinding.inflate(LayoutInflater.from(context), this, true)

    fun attachToPinInput(view: PinInputLayout) {
        val padListener: (View) -> Unit = {
            view.addChar((it as TextView).text[0])
        }

        binding.numOne.setOnClickListener(padListener)
        binding.numTwo.setOnClickListener(padListener)
        binding.numThree.setOnClickListener(padListener)
        binding.numFour.setOnClickListener(padListener)
        binding.numFive.setOnClickListener(padListener)
        binding.numSix.setOnClickListener(padListener)
        binding.numSeven.setOnClickListener(padListener)
        binding.numEight.setOnClickListener(padListener)
        binding.numNine.setOnClickListener(padListener)
        binding.numZero.setOnClickListener(padListener)

        binding.numDelete.setOnClickListener {
            view.removeLastChar()
        }

        binding.numTogglePass.setOnClickListener {
            view.toggleHideContent()
            binding.numTogglePass.setImageResource(
                if (view.isContentHidden)
                    R.drawable.ic_auth_input_toggle_password_off
                else
                    R.drawable.ic_auth_input_toggle_password_on
            )
        }
    }
}
