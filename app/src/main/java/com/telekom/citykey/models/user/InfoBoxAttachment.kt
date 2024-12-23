package com.telekom.citykey.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InfoBoxAttachment(
    @SerializedName("attachmentLink")
    val link: String,
    @SerializedName("attachmentText")
    val name: String
) : Parcelable
