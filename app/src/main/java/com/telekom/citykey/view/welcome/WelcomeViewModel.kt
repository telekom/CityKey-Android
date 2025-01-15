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

package com.telekom.citykey.view.welcome

import androidx.lifecycle.LiveData
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.BaseViewModel

class WelcomeViewModel(
    private val preferencesHelper: PreferencesHelper
) : BaseViewModel() {

    private val _confirmTrackingTerms: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val confirmTrackingTerms: LiveData<Boolean> get() = _confirmTrackingTerms

    init {
        initTrackingTerms()
    }

    private fun initTrackingTerms() {
        _confirmTrackingTerms.postValue(preferencesHelper.isTrackingConfirmed)
    }

    fun onSkipBtnClicked() {
        preferencesHelper.setFirstTimeFinished()
    }
}
