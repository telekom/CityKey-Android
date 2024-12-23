package com.telekom.citykey.view.user.profile.settings.personal_settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.view.BaseViewModel

class PersonalDetailSettingsViewModel(userInteractor: UserInteractor) : BaseViewModel() {

    val userPersonal: LiveData<UserProfile> get() = _userPersonal

    private val _userPersonal: MutableLiveData<UserProfile> = MutableLiveData()

    init {
        launch {
            userInteractor.user
                .subscribe {
                    if (it is UserState.Present) {
                        _userPersonal.postValue(it.profile)
                    }
                }
        }
    }
}
