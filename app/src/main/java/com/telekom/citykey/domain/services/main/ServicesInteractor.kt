package com.telekom.citykey.domain.services.main

import android.annotation.SuppressLint
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.*
import com.telekom.citykey.models.egov.DetailHelpInfo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class ServicesInteractor(
    private val globalData: GlobalData,
    private val cityRepository: CityRepository,
    private val servicesRepository: ServicesRepository
) {
    val state: Flowable<ServicesStates>
        get() = Observable.combineLatest(
            _state, globalData.user,
            BiFunction<ServicesStates, UserState, Pair<ServicesStates, UserState>> { state, user -> return@BiFunction state to user }
        )
            .map(this::setServicesRestrictions)
            .hide()
            .toFlowable(BackpressureStrategy.LATEST)
    val errors: Observable<Throwable> get() = _errors.hide()

    private val _state: BehaviorSubject<ServicesStates> = BehaviorSubject.create()
    private val _errors: PublishSubject<Throwable> = PublishSubject.create()
    private var lastCityId = -1
    private var refreshServicesDisposable: Disposable? = null
    private val servicesHelpInfo = mutableMapOf<Int, DetailHelpInfo>()

    fun getInfo(serviceId: Int): Maybe<DetailHelpInfo> =
        if (servicesHelpInfo[serviceId] != null) {
            Maybe.just(servicesHelpInfo[serviceId]!!)
        } else {
            servicesRepository.getServiceDetailInfo(globalData.currentCityId, serviceId)
                .doOnSuccess { servicesHelpInfo[serviceId] = it }
                .observeOn(AndroidSchedulers.mainThread())
        }

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    fun observeCity() {
        globalData.city
            .doOnNext {
                servicesHelpInfo.clear()
                if (it.cityId != lastCityId) {
                    _state.onNext(ServicesStates.Loading)
                    lastCityId = it.cityId
                } else if (_state.value == ServicesStates.Error) {
                    _state.onNext(ServicesStates.Loading)
                }
            }
            .filter { it.cityConfig?.showServicesOption ?: false }
            .subscribe(this::refreshServices)
    }

    private fun refreshServices(city: City) {
        refreshServicesDisposable?.dispose()
        refreshServicesDisposable = getServicesData(city)
            .map { convertResponseToData(it) }
            .map { ServicesStates.Success(it) }
            .doFinally { globalData.markGetServicesCompleted() }
            .subscribe(_state::onNext, this::evaluateError)
    }

    private fun getServicesData(city: City) = cityRepository.getServices(city)

    private fun convertResponseToData(response: Pair<List<ServicesResponse>, City>): ServicesData {
        val services = mutableListOf<CitizenService>()
        response.first.forEach { cityServices ->
            cityServices.cityServiceCategoryList.forEach { services.addAll(it.cityServiceList) }
        }

        return ServicesData(
            services,
            createViewTypes(response.second.cityConfig)
        )
    }

    private fun createViewTypes(config: CityConfig?): List<Int> {
        if (config == null) return listOf()
        val viewTypes = mutableListOf<Int>()
        if (config.showOurServices)
            viewTypes.add(ServicesViewTypes.VIEW_TYPE_OUR_SERVICES)
        if (config.showFavouriteServices)
            viewTypes.add(ServicesViewTypes.VIEW_TYPE_FAVORITES)
        if (config.showNewServices)
            viewTypes.add(ServicesViewTypes.VIEW_TYPE_NEW_SERVICES)
        if (config.showMostUsedServices)
            viewTypes.add(ServicesViewTypes.VIEW_TYPE_MOST_USED)
        if (config.showCategories)
            viewTypes.add(ServicesViewTypes.VIEW_TYPE_CATEGORIES)

        return viewTypes
    }

    private fun evaluateError(throwable: Throwable) {
        when (_state.value) {
            is ServicesStates.Success -> _errors.onNext(throwable)
            else -> _state.onNext(ServicesStates.Error)
        }
    }

    private fun setServicesRestrictions(pair: Pair<ServicesStates, UserState>): ServicesStates {
        if (pair.first !is ServicesStates.Success) return pair.first
        val servicesData = (pair.first as ServicesStates.Success).data
        servicesData.services.forEach {
            it.loginLocked = it.restricted && pair.second is UserState.Absent
            if (it.residence && pair.second is UserState.Present && !globalData.isUserBrowsingHomeCity) {
                it.cityLocked = globalData.userCityName ?: ""
            }
        }
        return ServicesStates.Success(servicesData)
    }
}
