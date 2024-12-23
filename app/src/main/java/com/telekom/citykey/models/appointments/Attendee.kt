package com.telekom.citykey.models.appointments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Attendee(
    val firstName: String,
    val lastName: String
) : Parcelable
