package com.telekom.citykey.models.appointments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Reason(
    val description: String,
    val sNumber: Int
) : Parcelable
