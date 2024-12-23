package com.telekom.citykey.domain.city.news

import android.annotation.SuppressLint
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.content.City
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class NewsInteractor(private val cityRepository: CityRepository, private val globalData: GlobalData) {

    private var refreshDisposable: Disposable? = null
    private var lastCityId = -1
    private var stickyNewsCount = 0

    private val _newsSubject: BehaviorSubject<NewsState> = BehaviorSubject.create()
    val newsObservable: Observable<NewsState> = _newsSubject.hide()

    private var _shouldUpdateWidget = false
    val shouldUpdateWidget get() = _shouldUpdateWidget

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        globalData.city.subscribe {
            stickyNewsCount = it.cityConfig?.stickyNewsCount ?: 0
            if (lastCityId != it.cityId) {
                lastCityId = it.cityId
                _newsSubject.onNext(NewsState.Loading)
            }
            refreshNews(it)
            _shouldUpdateWidget = true
        }
    }

    private fun refreshNews(city: City) {
        refreshDisposable?.dispose()
        refreshDisposable = cityRepository.getNews(city.cityId)
            .subscribe(_newsSubject::onNext, this::onError)
    }

    fun updateWidgetDone() {
        _shouldUpdateWidget = false
    }

    fun mapContent(stateItem: NewsState): NewsState {
        if (stateItem !is NewsState.Success)
            return stateItem

        val allNews = stateItem.content
        var stickyNews = allNews.filter { it.sticky }
            .toMutableList()

        // Fill sticky news if they are less than expected
        if (stickyNews.size < stickyNewsCount) {
            allNews.takeWhile {
                if (!stickyNews.contains(it)) stickyNews.add(it)
                stickyNews.size < stickyNewsCount
            }
        }

        // If the number of news is bigger than expected we take first ones
        stickyNews = stickyNews.take(stickyNewsCount).toMutableList()
        return NewsState.Success(stickyNews)
    }

    private fun onError(throwable: Throwable) {
        if (_newsSubject.value !is NewsState.Success)
            when (throwable) {
                is NetworkException -> {
                    (throwable.error as OscaErrorResponse).errors.forEach {
                        when (it.errorCode) {
                            ErrorCodes.ACTION_NOT_AVAILABLE -> {
                                _newsSubject.onNext(NewsState.ActionError)
                            }
                            else -> {
                                _newsSubject.onNext(NewsState.Error)
                            }
                        }
                    }
                }
            }
    }
}
