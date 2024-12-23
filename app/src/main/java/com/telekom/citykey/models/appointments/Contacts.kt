package com.telekom.citykey.models.appointments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Contacts(
    val contactDesc: String,
    val email: String,
    val telefon: String,
    val contactNotes: String
) : Parcelable
