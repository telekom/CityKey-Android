package com.telekom.citykey.models.api.requests

import com.google.gson.annotations.SerializedName

class DefectRequest(
    val lastName: String,
    val wasteBinId: String,
    val firstName: String,
    @SerializedName("service_code") val serviceCode: String,
    val lat: String,
    val long: String,
    val email: String,
    val description: String,
    @SerializedName("media_url") var mediaUrl: String,
    @SerializedName("sub_service_code") var subServiceCode: String,
    var houseNumber: String = "",
    var location: String = "",
    var phoneNumber: String,
    var postalCode: String = "",
    var streetName: String = ""
)
