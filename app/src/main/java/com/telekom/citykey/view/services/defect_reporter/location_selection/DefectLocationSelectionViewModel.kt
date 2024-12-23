package com.telekom.citykey.view.services.defect_reporter.location_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.view.NetworkingViewModel
import timber.log.Timber

class DefectLocationSelectionViewModel(
    private val globalData: GlobalData,
    private val locationInteractor: LocationInteractor
) : NetworkingViewModel() {
    val cityLocation: LiveData<LatLng> get() = _cityLocation

    val location: LiveData<LatLng?> get() = _location

    private val _location: MutableLiveData<LatLng?> = MutableLiveData()
    private val _cityLocation: MutableLiveData<LatLng> = MutableLiveData(globalData.cityLocation)

    init {
        launch {
            globalData.city.map { it.location }
                .subscribe(_cityLocation::postValue)
        }
    }

    fun onLocationPermissionAvailable() {
        launch {
            locationInteractor.getLocation()
                .subscribe(_location::postValue, Timber::e)
        }
    }
}
