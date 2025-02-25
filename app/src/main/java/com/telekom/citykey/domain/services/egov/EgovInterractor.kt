/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

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
