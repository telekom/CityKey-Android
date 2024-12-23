package com.telekom.citykey.custom.views.inputfields

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OscaInputLayoutState(
    val text: String,
    val validation: FieldValidation,
    val viewState: Parcelable?,
    val test: Int
) : Parcelable
