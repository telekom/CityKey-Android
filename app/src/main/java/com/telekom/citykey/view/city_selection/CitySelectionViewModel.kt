package com.telekom.citykey.view.city_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationBasedCitiesInteractor
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.content.AvailableCity
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CitySelectionViewModel(
    private val globalData: GlobalData,
    private val preferencesHelper: PreferencesHelper,
    private val availableCitiesInteractor: AvailableCitiesInteractor,
    private val locationBasedCitiesInteractor: LocationBasedCitiesInteractor,
    private val adjustManager: AdjustManager
) : NetworkingViewModel() {

    val contentAll: LiveData<List<Cities>> get() = _contentAll
    val cityUpdated: LiveData<Boolean> get() = _cityUpdated
    val isCityActive: LiveData<Unit> get() = _isCityActive

    private val _isCityActive: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _contentAll: MutableLiveData<List<Cities>> = MutableLiveData()
    private val _cityUpdated: MutableLiveData<Boolean> = MutableLiveData()
    private val availableCities = mutableListOf<AvailableCity>()
    private var cityItemNearest: Cities = Cities.Progress

    init {
        loadAvailableCities()
    }

    fun selectCity(availableCity: AvailableCity) {
        launch {
            globalData.loadCity(availableCity)
                .retryOnError(
                    this::onError,
                    retryDispatcher,
                    pendingRetries
                )
                .subscribe({
                    adjustManager.trackEvent(R.string.switch_city)
                    _cityUpdated.postValue(true)
                }, ::onError)
        }
    }

    private fun loadAvailableCities() {
        launch {
            availableCitiesInteractor.availableCities
                .subscribeOn(Schedulers.io())
                .map(this::setSelectedCity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        availableCities.clear()
                        availableCities.addAll(it)
                        updateCitiesContent()
                    },
                    this::onError
                )
        }
    }

    private fun updateCitiesContent() {
        val cities: MutableList<Cities> = availableCities.map { availableCity -> Cities.City(availableCity) }
            .toMutableList()
        cities.add(0, Cities.Header(R.string.c_002_cities_location_header))
        cities.add(1, cityItemNearest)
        cities.add(2, Cities.Header(R.string.c_002_city_selection_list_header))
        _contentAll.postValue(cities)
    }

    fun onPermissionsMissing() {
        cityItemNearest = Cities.NoPermission
        updateCitiesContent()
    }

    fun onNearestCityRequested() {
        launch {
            locationBasedCitiesInteractor.getNearestCity()
                .flatMap { nearestCity ->
                    val availableCity = availableCities.find { nearestCity.cityId == it.cityId }
                    return@flatMap if (availableCity == null) {
                        Maybe.error(Exception())
                    } else {
                        availableCity.distance = nearestCity.distance
                        Maybe.just(availableCity)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    { availableCity ->
                        cityItemNearest = Cities.NearestCity(availableCity)
                        updateCitiesContent()
                    },
                    this::onLocationBasedCitiesError
                )
        }
    }

    fun onLocationServicesDisabled() {
        cityItemNearest = Cities.Error
        updateCitiesContent()
    }

    private fun setSelectedCity(cities: List<AvailableCity>): List<AvailableCity> {
        val selectedCityId = preferencesHelper.getSelectedCityId()

        cities.find { it.cityId == selectedCityId }?.let {
            cities.forEach { city -> city.isSelected = false }
            it.isSelected = true
        }
        return cities
    }

    private fun onLocationBasedCitiesError(throwable: Throwable) {
        cityItemNearest = Cities.Error
        updateCitiesContent()
        onError(throwable)
    }

    private fun onError(throwable: Throwable) {
        cityItemNearest = Cities.Error
        updateCitiesContent()
        when (throwable) {
            is NoConnectionException -> {
                showRetry()
            }
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.SERVICE_NOT_ACTIVE -> {
                            _isCityActive.postValue(Unit)
                        }
                    }
                }
            }
            else -> {
                _technicalError.postValue(Unit)
            }
        }
    }

    fun getUpdatedCitiesList(emailSupportIsAvailable: Boolean, cities: List<Cities>) =
        if (emailSupportIsAvailable && cities.size > 3) {
            cities.toMutableList().apply { add(Cities.ContactLink) }
        } else {
            cities
        }

}
