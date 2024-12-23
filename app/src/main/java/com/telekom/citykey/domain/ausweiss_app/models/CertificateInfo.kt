package com.telekom.citykey.domain.ausweiss_app.models

data class CertificateInfo(
    val issuerName: String,
    val issuerUrl: String,
    val purpose: String,
    val subjectName: String,
    val subjectUrl: String,
    val termsOfUsage: String
) {
    val subject get() = "$subjectName\n$subjectUrl"
    val issuer get() = "$issuerName\n$issuerUrl"
}
