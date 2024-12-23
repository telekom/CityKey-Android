package com.telekom.citykey.models.content

import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.utils.extensions.tryParsingColor

data class City(
    val cityId: Int,
    val cityName: String? = null,
    val cityColor: String? = null,
    val stateName: String? = null,
    val country: String? = null,
    val cityPicture: String? = null,
    val cityPreviewPicture: String? = null,
    val cityNightPicture: String? = null,
    val servicePicture: String? = null,
    val municipalCoat: String? = null,
    val serviceDesc: String? = null,
    val imprintDesc: String? = null,
    val imprintImage: String? = null,
    val imprintLink: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val postalCode: String? = null,
    val cityConfig: CityConfig? = null
) {
    val cityColorInt: Int get() = tryParsingColor(cityColor)
    val location: LatLng get() = LatLng(latitude, longitude)
}
