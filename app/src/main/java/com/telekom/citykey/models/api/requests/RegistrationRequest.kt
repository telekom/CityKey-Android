package com.telekom.citykey.models.api.requests

@Suppress("unused")
class RegistrationRequest(
    val password: String,
    val postalCode: String,
    val email: String,
    val dateOfBirth: String?
)
