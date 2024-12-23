package com.telekom.citykey.models.content

data class CitizenServiceCategory(
    val category: String,
    val categoryId: Int,
    val image: String,
    val icon: String,
    val cityServiceList: List<CitizenService>
)
