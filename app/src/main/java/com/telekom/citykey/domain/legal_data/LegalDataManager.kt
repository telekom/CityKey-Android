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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

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
