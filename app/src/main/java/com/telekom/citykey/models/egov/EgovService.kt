package com.telekom.citykey.models.egov

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EgovService(
    val isFavorite: Boolean,
    val serviceName: String,
    val serviceDetail: String,
    val shortDescription: String,
    val longDescription: String,
    val serviceId: String,
    val searchKey: List<String>?,
    val subServices: List<EgovSubService>,
    val linksInfo: List<EgovLinkInfo>
) : Parcelable

@Parcelize
data class EgovLinkInfo(
    val link: String,
    val title: String,
    val linkType: String
) : Parcelable
