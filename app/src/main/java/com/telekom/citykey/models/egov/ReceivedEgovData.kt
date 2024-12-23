package com.telekom.citykey.models.egov

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EgovData(
    val groupInfo: List<GroupInfo>,
    val services: List<ServiceData>
) : Parcelable

@Parcelize
data class GroupInfo(
    val groupId: Int,
    val groupName: String,
    val groupIcon: String
) : Parcelable


@Parcelize
data class ServiceData(
    val isFavorite: Boolean,
    val serviceName: String,
    val serviceDetail: String,
    val shortDescription: String,
    val longDescription: String,
    val serviceId: String,
    val searchKey: List<String>?,
    val subServices: List<EgovSubService>,
    val linksInfo: List<EgovLinkInfo>,
    val groupIds: List<Int>
) : Parcelable