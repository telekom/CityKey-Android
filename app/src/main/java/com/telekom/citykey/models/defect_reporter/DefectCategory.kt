package com.telekom.citykey.models.defect_reporter

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DefectCategory(
    @SerializedName("service_code")
    val serviceCode: String,
    @SerializedName("service_name")
    val serviceName: String?,
    val description: String?,
    @SerializedName("sub_categories")
    val subCategories: List<DefectSubCategory>?
) : Parcelable

@Parcelize
data class DefectSubCategory(
    @SerializedName("service_code")
    val serviceCode: String,
    @SerializedName("service_name")
    val serviceName: String?,
    val description: String?,
    @SerializedName("additional_info")
    val hasAdditionalInfo: Boolean?
) : Parcelable
