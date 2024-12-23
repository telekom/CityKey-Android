package com.telekom.citykey.models.content

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
class UserProfile(
    val dateOfBirth: Date?,
    val email: String,
    val accountId: String,
    var postalCode: String,
    var cityName: String,
    var homeCityId: Int,
    val dpnAccepted: Boolean,
    val isCspUser: Boolean?,
    val wasteTypeId: List<CitizenService>? = null
): Parcelable
