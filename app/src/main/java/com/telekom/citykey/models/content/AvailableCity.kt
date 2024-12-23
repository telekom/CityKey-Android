package com.telekom.citykey.models.content

data class AvailableCity(
    val cityId: Int,
    val cityName: String? = null,
    val cityColor: String? = null,
    val stateName: String? = null,
    val country: String? = null,
    val cityPicture: String? = null,
    val cityPreviewPicture: String? = null,
    val servicePicture: String? = null,
    val municipalCoat: String? = null,
    val postalCode: List<String>? = null
) {
    var distance: Int = 0
    var isSelected: Boolean = false
}
