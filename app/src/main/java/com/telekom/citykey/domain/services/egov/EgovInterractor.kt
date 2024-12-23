package com.telekom.citykey.domain.services.egov

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.models.egov.EgovGroup
import com.telekom.citykey.models.egov.EgovService
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class EgovInterractor(
    private val globalData: GlobalData,
    private val servicesRepository: ServicesRepository,
    private val prefs: SharedPreferences
) {

    companion object {
        private const val PREFS_HISTORY_KEY = "EGOV_SEARCH_HISTORY_KEY_C"
        private const val MAX_HISTORY_ELEMENTS = 10
    }

    private val egovGroups = mutableListOf<EgovGroup>()
    private val _egovStateSubject: BehaviorSubject<EgovState> = BehaviorSubject.createDefault(EgovState.LOADING)
    private val _egovSearchResults: PublishSubject<String> = PublishSubject.create()

    val egovStateObservable: Observable<EgovState> get() = _egovStateSubject.hide()
    val egovSearchResultsObservable: Observable<List<EgovSearchItems>>
        get() = _egovSearchResults.debounce(500L, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .map(::performSearch)
            .observeOn(AndroidSchedulers.mainThread())

    private val searchHistory: MutableList<String>
        get() = (prefs.getString(PREFS_HISTORY_KEY + globalData.currentCityId, "") ?: "")
            .trim()
            .split(';')
            .filter { it.isNotBlank() }
            .takeLast(MAX_HISTORY_ELEMENTS)
            .toMutableList()
    private val searchHistoryItems: List<EgovSearchItems>
        get() = if (searchHistory.isEmpty()) {
            listOf(EgovSearchItems.FullScreenMessage(R.string.egov_search_ftu_msg))
        } else
            mutableListOf<EgovSearchItems>(EgovSearchItems.Header(R.string.egov_search_last_searches_label))
                .apply {
                    addAll(searchHistory.reversed().map { EgovSearchItems.History(it) })
                }

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        globalData.city
            .distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
            .subscribe {
                egovGroups.clear()
                _egovStateSubject.onNext(EgovState.LOADING)
            }
    }

    fun loadEgovItems(): Completable {
        return if (egovGroups.isEmpty())
            servicesRepository.getEgovItems(globalData.currentCityId)
                .doOnSubscribe { _egovStateSubject.onNext(EgovState.LOADING) }
                .doOnError { _egovStateSubject.onNext(EgovState.ERROR) }
                .doOnSuccess { items ->
                    egovGroups.addAll(items)
                    _egovStateSubject.onNext(EgovState.Success(items))
                }
                .ignoreElement()
                .observeOn(AndroidSchedulers.mainThread())
        else Completable.complete()
    }

    fun loadEgovGroupData(groupId: Int): EgovGroup? {
        return egovGroups.firstOrNull { it.groupId == groupId }
    }

    fun searchForKeywords(query: String) {
        _egovSearchResults.onNext(query)
    }

    private fun performSearch(query: String): List<EgovSearchItems> {
        if (query.trim().length < 3) return searchHistoryItems
        val keywords: List<String> = query.trim().lowercase().split(' ')
        val egovServices = egovGroups.flatMap { it.services }
        val results = mutableListOf<EgovService>()
        val keywordsToFind = keywords.size

        egovServices.forEach { service ->
            var keywordsFound = 0

            keywords.forEach {
                if (service.serviceName.lowercase().contains(it) || service.shortDescription.lowercase()
                        .contains(it) || service.longDescription.lowercase().contains(it)
                )
                    keywordsFound++
            }

            if (keywordsFound == keywordsToFind) results.add(service)
        }

        egovServices.forEach { service ->
            var keywordsFound = 0
            keywords.forEach { keyword ->
                service.searchKey?.forEach keyWordSearch@{
                    if (it.lowercase().contains(keyword.lowercase())) {
                        keywordsFound++
                        return@keyWordSearch
                    }
                }
            }
            if (keywordsFound >= keywordsToFind) results.add(service)
        }

        egovGroups.forEach { group ->
            var keywordsFound = 0

            keywords.forEach {
                if (group.groupName.lowercase().contains(it))
                    keywordsFound++
            }

            if (keywordsFound == keywordsToFind) results.addAll(group.services)
        }

        return if (results.isEmpty()) {
            listOf(EgovSearchItems.FullScreenMessage(R.string.egov_search_no_results_format, query))
        } else {
            mutableListOf<EgovSearchItems>(EgovSearchItems.Header(R.string.egov_search_results_label))
                .apply {
                    addAll(results.distinct().sortedBy { it.serviceName }.map { EgovSearchItems.Result(it) })
                }
        }
    }

    fun saveServiceInHistory(service: EgovService) {
        prefs.edit {
            val set = searchHistory
                .apply {
                    remove(service.serviceName)
                    add(service.serviceName)
                }
                .joinToString(";")

            putString(PREFS_HISTORY_KEY + globalData.currentCityId, set)
        }
    }
}
