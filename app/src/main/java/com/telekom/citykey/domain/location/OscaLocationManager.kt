package com.telekom.citykey.domain.location

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class OscaLocationManager(private val geocoder: Geocoder) {

    fun getLatLngFromAddress(strAddress: String): Single<LatLng> {
        return Single.create<LatLng> {
            val address = geocoder.getFromLocationName(strAddress, 1)?.get(0)
            it.onSuccess(LatLng(address!!.latitude, address.longitude))
        }
            .subscribeOn(Schedulers.io())
    }

    fun getAddressFromLatLng(addressLat: Double, addressLong: Double): Maybe<Address> {
        return Maybe.create<Address> {
            val address = geocoder.getFromLocation(addressLat, addressLong, 1)?.get(0)
            if (address != null) {
                it.onSuccess(address)
            }
        }.subscribeOn(Schedulers.io())
    }
}
