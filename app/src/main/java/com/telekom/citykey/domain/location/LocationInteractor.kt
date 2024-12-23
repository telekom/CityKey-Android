package com.telekom.citykey.domain.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import io.reactivex.Single

class LocationInteractor(private val client: FusedLocationProviderClient) {

    private val cancellationTokenSource = CancellationTokenSource()

    @SuppressLint("MissingPermission")
    fun getLocation(): Single<LatLng> = Single.create {
        client.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnCompleteListener { task ->
            if (it.isDisposed) return@addOnCompleteListener

            if (task.isSuccessful && task.result != null) {
                val result: Location = task.result
                it.onSuccess(LatLng(result.latitude, result.longitude))
            } else {
                it.onError(Exception())
            }
        }
    }

    fun cancelLocation() {
        cancellationTokenSource.cancel()
    }
}
