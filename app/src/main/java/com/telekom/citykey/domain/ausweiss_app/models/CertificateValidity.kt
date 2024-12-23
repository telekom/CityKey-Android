package com.telekom.citykey.domain.ausweiss_app.models

class CertificateValidity(
    private val effectiveDate: String,
    private val expirationDate: String
) {
    val validity get() = "$effectiveDate - $expirationDate"
}
