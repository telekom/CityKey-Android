package com.telekom.citykey.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class InfoBoxContent(
    val attachments: List<InfoBoxAttachment>,
    val buttonAction: String?,
    val buttonText: String?,
    val category: InfoBoxCategory,
    val creationDate: Date,
    val description: String,
    val details: String,
    val headline: String,
    var isRead: Boolean,
    val userInfoId: Int,
    val messageId: Int
) : Parcelable
