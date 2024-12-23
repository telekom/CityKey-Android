package com.telekom.citykey.models.fahrradparken

import com.google.gson.annotations.SerializedName

data class FahrradparkenReport(
    @SerializedName("service_request_id") var serviceRequestId: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("lat") var lat: Double? = null,
    @SerializedName("long") var lng: Double? = null,
    @SerializedName("service_name") var serviceName: String? = null,
    @SerializedName("media_url") var mediaUrl: String? = null,
    @SerializedName("extended_attributes") var extendedAttributes: ExtendedAttributes? = null
)

data class ExtendedAttributes(
    @SerializedName("markaspot") var markASpot: MarkASpot? = null
)

data class MarkASpot(
    @SerializedName("status_descriptive_name") var statusDescriptiveName: String? = null,
    @SerializedName("status_hex") var statusHex: String? = null,
    @SerializedName("status_icon") var statusIcon: String? = null
)
