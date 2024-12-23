package com.telekom.citykey.custom.views.inputfields

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FieldValidation(
    val state: Int,
    val message: String?,
    val stringRes: Int = -1
) : Parcelable {
    companion object {
        const val IDLE = 0
        const val OK = 1
        const val NOT_OK = 2
        const val ERROR = 3
        const val SUCCESS = 4
        const val HELPER = 5
    }
}
