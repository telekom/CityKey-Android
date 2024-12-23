package com.telekom.citykey.domain.ausweiss_app.models

class AccessRightPayload(
    val effective: List<String>,
    val optional: List<String>? = null,
    val required: List<String>? = null
)
