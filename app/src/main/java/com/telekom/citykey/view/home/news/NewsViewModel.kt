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

package com.telekom.citykey.view.home.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.view.BaseViewModel

class NewsViewModel(private val newsInteractor: NewsInteractor) : BaseViewModel() {

    val news: LiveData<List<CityContent>> get() = _news

    private val _news: MutableLiveData<List<CityContent>> = MutableLiveData()

    init {
        launch {
            newsInteractor.newsObservable.filter { it is NewsState.Success }
                .map { it as NewsState.Success }
                .map { it.content }
                .subscribe(_news::postValue)
        }
    }
}
