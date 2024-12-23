package com.telekom.citykey.domain.services.poi

import android.annotation.SuppressLint
import com.google.android.gms.maps.model.LatLngBounds
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.models.poi.PoiCategory
import com.telekom.citykey.models.poi.PoiCategoryGroup
import com.telekom.citykey.models.poi.PoiData
import com.telekom.citykey.models.poi.PointOfInterest
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.view.services.poi.PoiState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

@SuppressLint("CheckResult")
class POIInteractor(
    private val servicesRepository: ServicesRepository,
    private val globalData: GlobalData,
    private val prefs: PreferencesHelper,
    private val locationInteractor: LocationInteractor
) {
    val poiData: Observable<PoiData> get() = _poisSubject.hide()
    val activeCategory: Observable<PoiCategory> get() = _activeCategory.hide()
    val poiState: Observable<PoiState> get() = _poiState.hide()

    private val _poisSubject: BehaviorSubject<PoiData> = BehaviorSubject.create()
    private val _activeCategory: BehaviorSubject<PoiCategory> = BehaviorSubject.create()
    private val _poiState: BehaviorSubject<PoiState> = BehaviorSubject.create()

    private val categories = mutableListOf<PoiCategoryGroup>()
    private var isLocationAvailable: Boolean = false
    var selectedCategory: PoiCategory? = null
    private var locationLat: Double = 0.0
    private var locationLong: Double = 0.0

    init {
        globalData.city
            .distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
            .subscribe {
                categories.clear()
                _poisSubject.onNext(PoiData(emptyList(), isLocationAvailable, null, globalData.cityLocation))
                selectedCategory = null
                _poiState.onNext(PoiState.LOADING)
            }
    }

    fun getCategories(): Observable<List<PoiCategoryGroup>> = if (categories.isEmpty()) {
        servicesRepository.getPoiCategories(globalData.currentCityId)
            .doOnSuccess(categories::addAll)
            .toObservable()
    } else {
        Observable.just(categories)
    }

    fun getPois(category: PoiCategory, isInitialLoading: Boolean): Completable =
        if (isInitialLoading) {
            locationInteractor.getLocation()
                .subscribeOn(Schedulers.io())
                .doOnSuccess { isLocationAvailable = true }
                .onErrorReturn {
                    isLocationAvailable = false
                    globalData.cityLocation
                }
                .flatMapMaybe {
                    locationLat = it.latitude
                    locationLong = it.longitude
                    servicesRepository.getPOIs(globalData.currentCityId, locationLat, locationLong, category.categoryId)
                }
                .doOnSubscribe { if (isInitialLoading) _poiState.onNext(PoiState.LOADING) }
                .doOnSuccess { processPois(it, category) }
                .doOnError { if (isInitialLoading) _poiState.onNext(PoiState.ERROR) }
                .ignoreElement()
                .observeOn(AndroidSchedulers.mainThread())
        } else {
            fetchPoisForLastAvailableLocation(category)
        }

    fun getSelectedPoiCategory() = prefs.getPoiCategory(globalData.cityName)

    private fun fetchPoisForLastAvailableLocation(category: PoiCategory): Completable =
        servicesRepository.getPOIs(globalData.currentCityId, locationLat, locationLong, category.categoryId)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { processPois(it, category) }
            .ignoreElement()
            .observeOn(AndroidSchedulers.mainThread())

    private fun processPois(pointOfInterestList: List<PointOfInterest>, category: PoiCategory) {
        pointOfInterestList.forEach { poi -> poi.categoryGroupIcon = category.categoryIcon }
        _poisSubject.onNext(createPoiData(pointOfInterestList))
        _activeCategory.onNext(category)
        selectedCategory = category
        prefs.savePoiCategory(globalData.cityName, category)
        _poiState.onNext(if (pointOfInterestList.isEmpty()) PoiState.EMPTY else PoiState.SUCCESS)
    }

    private fun createPoiData(items: List<PointOfInterest>) =
        when {
            items.size < 50 -> {
                val bounds = LatLngBounds.builder()
                items.forEach { poi -> bounds.include(poi.latLang) }
                PoiData(items, isLocationAvailable, bounds.build(), globalData.cityLocation)
            }
            items.size > 150 -> {
                PoiData(items, isLocationAvailable, null, globalData.cityLocation, 15f)
            }
            else -> {
                PoiData(items, isLocationAvailable, null, globalData.cityLocation)
            }
        }
}
