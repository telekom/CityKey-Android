package com.telekom.citykey.models.api.requests

class TpnsRegisterRequest(
    val applicationType: String = "AOS",
    val applicationKey: String,
    val deviceId: String,
    val deviceRegistrationId: String,
    val additionalParameters: List<TpnsParam>
)

class TpnsParam(val key: String, val value: String)
