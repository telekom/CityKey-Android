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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.services.citizen_surveys

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.services.surveys.SurveysInteractor
import com.telekom.citykey.domain.services.surveys.SurveysState
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.isInFuture
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SurveysOverviewViewModel(
    private val surveysInteractor: SurveysInteractor, private val preferencesHelper: PreferencesHelper
) : BaseViewModel() {

    val listItems: LiveData<List<SurveyListItem>> get() = _listItems
    val state: LiveData<SurveysState> get() = surveysInteractor.state
    val stopRefresh: LiveData<Unit> get() = _stopRefresh

    private val _listItems: MutableLiveData<List<SurveyListItem>> = MutableLiveData()
    private val _stopRefresh: MutableLiveData<Unit> = SingleLiveEvent()

    init {
        launch {
            surveysInteractor.surveys.subscribeOn(Schedulers.io()).map { surveys ->
                val runningSurveys =
                    surveys.filter { it.daysLeft > 0 && it.startDate.isInFuture.not() }.map(SurveyListItem::Item)
                val finishedSurveys =
                    surveys.filter { it.daysLeft == 0 && it.startDate.isInFuture.not() }.map(SurveyListItem::Item)
                val upcomingSurveys = surveys.filter { it.startDate.isInFuture }.map(SurveyListItem::Item)

                val listItems = mutableListOf<SurveyListItem>()

                listItems.add(SurveyListItem.Header(R.string.cs_002_running_list_header))
                if (runningSurveys.isNotEmpty()) listItems.addAll(runningSurveys)
                else listItems.add(SurveyListItem.NoRunningSurveys)

                if (upcomingSurveys.isNotEmpty()) {
                    listItems.add(SurveyListItem.Header(R.string.cs_002_upcoming_list_header))
                    listItems.addAll(upcomingSurveys)
                }

                if (finishedSurveys.isNotEmpty()) {
                    listItems.add(SurveyListItem.Header(R.string.cs_002_closed_list_header))
                    listItems.addAll(finishedSurveys)
                }
                return@map listItems
            }.observeOn(AndroidSchedulers.mainThread())
                .subscribe(_listItems::postValue)
        }
    }

    fun shouldNavigateToSurveyDetails(): Boolean {
        return preferencesHelper.isPreviewMode.not() or (preferencesHelper.isPreviewMode && surveysInteractor.isSurveyPreviewAvailable())
    }

    fun isPreview(): Boolean = preferencesHelper.isPreviewMode

    fun onRefreshRequested() {
        launch {
            surveysInteractor.refreshSurveys().observeOn(AndroidSchedulers.mainThread())
                .subscribe { _stopRefresh.postValue(Unit) }
        }
    }
}
