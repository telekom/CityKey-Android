package com.telekom.citykey.domain.global_messager

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

class GlobalMessages(private val context: Context) {

    fun displayToast(@StringRes resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
    }
}
