package com.telekom.citykey.models.user

import com.google.gson.annotations.SerializedName

data class Credentials(
    @SerializedName("access_token") var accessToken: String,
    @SerializedName("refresh_token") var refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: Int,
    var birthday: Long,
    var save: Boolean = false
)
