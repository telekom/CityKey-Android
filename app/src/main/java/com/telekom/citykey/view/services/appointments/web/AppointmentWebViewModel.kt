package com.telekom.citykey.view.services.appointments.web

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.utils.DateUtil.FORMAT_DD_MM_YYYY
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AppointmentWebViewModel(
    private val userInteractor: UserInteractor,
    private val globalData: GlobalData
) : BaseViewModel() {

    val userDataPost: LiveData<String> get() = _userDataPost
    val userIsLoggedOut: LiveData<Boolean> get() = _userIsLoggedOut
    val callbackUrl = "http://citykey.callback.url/"

    private val _userDataPost = MutableLiveData<String>()
    private val _userIsLoggedOut = MutableLiveData<Boolean>()

    fun onGivePermissionClicked(includeUserData: Boolean, serviceParmas: Map<String, String>) {
        if (!includeUserData) {
            _userDataPost.value = createQueryString(null, serviceParmas)
        } else {
            launch {
                userInteractor.user
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it is UserState.Present) {
                            _userDataPost.value = createQueryString(it.profile, serviceParmas)
                        } else {
                            _userIsLoggedOut.value = true
                        }
                    }
            }
        }
    }

    private fun createQueryString(userProfile: UserProfile?, serviceParmas: Map<String, String>): String {
        val resultMap = createInitialParameterMap(serviceParmas)
        val email = serviceParmas["email"].toString()
        val dob = serviceParmas["date_of_birth"].toString()

        userProfile?.let {
            resultMap[email] = it.email
            resultMap["city"] = it.cityName
            resultMap["zip"] = it.postalCode
            if (it.dateOfBirth != null) {
                resultMap[dob] = it.dateOfBirth.toDateString(FORMAT_DD_MM_YYYY)
            }
        }
        return generateQueryString(resultMap)
    }

    private fun createInitialParameterMap(serviceParms: Map<String, String>): HashMap<String, String> {
        val token = serviceParms["entry_token"].toString()

        val resultMap = hashMapOf("entry_mode" to "app_citykey")
        resultMap["ident"] = globalData.cityName + "#" + userInteractor.userId.toString()
        resultMap["entry_token"] = token
        resultMap["return_url"] = callbackUrl
        resultMap["env"] = BuildConfig.KOMMUNIX_ENV_REQUEST
        return resultMap
    }

    private fun generateQueryString(inputMap: Map<String, String>): String {

        val builder = StringBuilder()

        for ((key, value) in inputMap) {
            builder.append("$key=$value&")
        }
        builder.deleteCharAt(builder.length - 1)
        return builder.toString()
    }
}
