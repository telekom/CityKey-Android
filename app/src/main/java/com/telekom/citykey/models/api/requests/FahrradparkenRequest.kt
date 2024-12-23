package com.telekom.citykey.models.api.requests

import com.google.gson.annotations.SerializedName

class FahrradparkenRequest(
    @SerializedName("service_code") val serviceCode: String,
    @SerializedName("sub_service_code") var subServiceCode: String,
    @SerializedName("media_url") var mediaUrl: String,
    val description: String,
    val email: String,
    val lat: String,
    val long: String,
    val firstName: String,
    val lastName: String
)
