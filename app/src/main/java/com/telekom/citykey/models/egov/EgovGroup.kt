package com.telekom.citykey.models.egov

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EgovGroup(
    val groupId: Int,
    val groupName: String,
    val groupIcon: String,
    val services: List<EgovService>
) : Parcelable
