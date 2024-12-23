package com.telekom.citykey.domain.legal_data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.models.content.Terms
import com.telekom.citykey.utils.PreferencesHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LegalDataManager(
    private val oscaRepository: OscaRepository,
    private val preferencesHelper: PreferencesHelper
) {
    private val _legalInfo = MutableLiveData<Terms>()
    val legalInfo: LiveData<Terms> get() = _legalInfo

    private var refresherDisposable: Disposable? = null

    fun loadLegalData(): Completable = Single.just(preferencesHelper.legalData)
        .subscribeOn(Schedulers.io())
        .flatMapCompletable { data ->
            if (data.isEmpty()) {
                oscaRepository.getLegalData()
                    .doOnSuccess(this::saveLegalData)
                    .doOnSuccess(_legalInfo::postValue)
                    .doOnError(Timber::e)
                    .ignoreElement()
            } else {
                refreshLegalData()
                Completable.fromAction {
                    _legalInfo.postValue(Gson().fromJson(data, Terms::class.java))
                }
            }
        }

    private fun saveLegalData(data: Terms) {
        preferencesHelper.saveLegalData(Gson().toJson(data))
    }

    private fun refreshLegalData() {
        refresherDisposable?.dispose()
        refresherDisposable = oscaRepository.getLegalData()
            .subscribe(
                {
                    saveLegalData(it)
                    _legalInfo.postValue(it)
                },
                Timber::e
            )
    }
}
