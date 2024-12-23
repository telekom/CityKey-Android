package com.telekom.citykey.models.appointments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Location(
    val houseNumber: String,
    val addressDesc: String,
    val place: String,
    val postalCode: String,
    val street: String
) : Parcelable
