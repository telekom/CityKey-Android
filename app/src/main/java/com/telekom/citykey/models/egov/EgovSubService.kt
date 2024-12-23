package com.telekom.citykey.models.egov

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EgovSubService(
    val link: String,
    val linkType: String,
    val isFavorite: Boolean,
    val subServiceName: String,
    val subServiceDetail: String,
    val subServiceDescription: String,
    val subServiceId: String,
    val subServiceOrder: Int
) : Parcelable
