package com.telekom.citykey.domain.ausweiss_app.models

class IdentMessage(
    val msg: String = "",
    val name: String = "",
    val url: String? = null,
    val reader: Reader? = null,
    val card: Card? = null,
    val result: Result? = null,
    val chat: AccessRightPayload,
    val validity: CertificateValidity,
    val description: CertificateInfo,
    val error: Any? = null
)
