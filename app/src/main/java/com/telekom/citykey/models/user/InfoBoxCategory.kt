package com.telekom.citykey.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InfoBoxCategory(
    @SerializedName("categoryIcon")
    val icon: String,
    @SerializedName("categoryName")
    val name: String
) : Parcelable
