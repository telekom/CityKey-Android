package com.telekom.citykey.models.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CitizenService(
    val serviceId: Int,
    val service: String,
    val image: String,
    @SerializedName("header_image") val headerImage: String?,
    val icon: String,
    val restricted: Boolean,
    val residence: Boolean,
    val description: String,
    val serviceType: String?,
    val isNew: Boolean,
    val templateId: Int?,
    var loginLocked: Boolean = false,
    var cityLocked: String?,
    var displayFavoredIcon: Boolean = false,
    var favored: Boolean = false,
    val function: String?,
    val rank: Int?,
    var helpLinkTitle: String? = null,
    val serviceParams: Map<String, String>?,
    val serviceAction: List<ServiceAction>?,
    var category: String? = null
) : Parcelable

@Parcelize
class ServiceAction(
    val androidUri: String,
    val action: Int?,
    val visibleText: String,
    val type: Int,
    val buttonDesign: Int,
    val actionOrder: Int,
    val actionType: String?
) : Parcelable
