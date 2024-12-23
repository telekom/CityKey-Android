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
