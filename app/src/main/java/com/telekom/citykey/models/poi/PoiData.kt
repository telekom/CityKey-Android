package com.telekom.citykey.models.poi

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class PoiData(
    val items: List<PointOfInterest>,
    val isLocationAvailable: Boolean,
    val bounds: LatLngBounds?,
    val cityLocation: LatLng,
    val zoomLevel: Float = 14f
)
