package com.telekom.citykey.models.waste_calendar

data class FtuWaste(
    val streetName: String,
    val houseNumberList: List<String>
)

data class Address(val streetName: String, val houseNumber: String)
